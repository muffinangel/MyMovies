package com.example.karot.mymovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView imageView;
    private String userUID;
    private static final String TAG = "AccountActivity";
    private Context mContext = this;
    private ListView listView;
    private ImageButton check;
    private ImageButton delete;

    private TextView aboutme;

    private UserAdapter uAdapter ;
    private ArrayList<User> usersList = new ArrayList<>();
    private User selected;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends_acc:
                    mTextMessage.setText(getString(R.string.navigation_friends_str));
                    updateUI(FriendsActivity.class);
                    return true;
                case R.id.navigation_favorites_acc:
                    mTextMessage.setText(getString(R.string.navigation_favorites_str));
                    updateUI(FavoritesActivity.class);
                    return true;
                case R.id.navigation_library_acc:
                    mTextMessage.setText(getString(R.string.navigation_library_str));
                    updateUI(LibraryActivity.class);
                    return true;
                case R.id.navigation_search_acc:
                    mTextMessage.setText(getString(R.string.navigation_search_str));
                    updateUI(SearchActivity.class);
                    return true;
            }
            return false;
        }
    };

    private void beginDownload() {
        // Get path
        String path = "avatars/cat001.jpeg" ;//+ mFileUri.getLastPathSegment(); //docelowo chcemy pobrać z bazy

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(this, MyDownloadService.class)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD);
        startService(intent);

        // Show loading spinner
        //showProgressDialog(getString(R.string.progress_downloading));
    }


    private void updateUI(Class cls) {
        Intent intent= new Intent(this, cls);
        intent.putExtra("CURRENT_USER", user);
        intent.putExtra("USER_ID", userUID);
        //intent.putExtra("CUREENT_AUTH", mAuth);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTitle("Account");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);



        Intent intent = getIntent();
        user = (FirebaseUser) intent.getExtras().getSerializable("CURRENT_USER"); // nie działa!!!
        userUID = intent.getStringExtra("USER_ID");
       // String userID = user.getUid(); // user jest nullem - autoryzować na nowo za każdym razem?


        uAdapter = new UserAdapter(this,usersList);
        listView = findViewById(R.id.list_of_friends_requests);
        check =findViewById(R.id.check);
        delete = findViewById(R.id.delete);
        check.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        aboutme = findViewById(R.id.aboutme);

        db.collection("users").document(userUID).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                aboutme.setText((String) document.get("about"));


                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

        db.collection("friends_requests").document(userUID).collection(userUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("ACCOUNT: FRIENDS REQSTS", document.getId() + " => " + document.getData());
                        User u = new  User((String) document.get("nick"), (String) document.get("description"), "", document.getId(), false);
                        usersList.add(u);
                        listView.setAdapter(uAdapter);
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                check.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                listView.setSelection(position);
                selected = usersList.get(position);
                // TODO highlighting - sprawdz zakladki
            }
        });

        check.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(selected != null) {
                    //1. dodaj do friends  - jeśli jeszcze nie dodany (nawet jeśli, to sie chyba nadpisze)

                    Map<String, Object> data = new HashMap<>();
                    data.put("nick", selected.getNick());
                    data.put("description", selected.getAbout());
                    db.collection("friends").document(selected.getUid()).collection(selected.getUid()).document(userUID).set(data);
                    db.collection("friends").document(userUID).collection(userUID).document(selected.getUid()).set(data);

                    // i usun z friends_requests
                    db.collection("friends_requests").document(userUID).collection(userUID).document(selected.getUid())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });

                    //2. usun z listy -
                    usersList.remove(selected);
                    selected = null;
                    check.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);

                    //3. wyswietl toasta
                    Toast.makeText(mContext,"added to friends", Toast.LENGTH_SHORT).show();

                    //4. wyswietl nowa liste
                    listView.setAdapter(uAdapter);

                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(selected != null) {
                    //1. usun friends requets

                    db.collection("friends_requests").document(userUID).collection(userUID).document(selected.getUid()).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });


                    //2. usun z listy -
                    usersList.remove(selected);
                    selected = null;
                    check.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);

                    //3. wyswietl toasta
                    Toast.makeText(mContext,"deleted this requests", Toast.LENGTH_SHORT).show();

                    //4. wyswietl nowa liste
                    listView.setAdapter(uAdapter);

                }
            }
        });







        //beginDownload();
        // Reference to an image file in Cloud Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("avatars/cat001.jpeg");
        // TODO moze to zmien

        // ImageView in your Activity
        imageView = findViewById(R.id.userPhoto);
        imageView.setImageResource(R.drawable.cat_movie);

        //doesnt work :(
        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        //GlideApp.with(this /* context */)
        //        .load(imageRef)
        //        .into(imageView);

        //doesnt work :(
        //StorageReference ref = FirebaseStorage.getInstance().getReference().child("ImagensExercicios/abdominal_1.bmp");
        //Glide.with(this)/*.using(new FirebaseImageLoader())*/.load(imageRef).into(imageView);


/*
        Task<Uri> dURL = storageReference.child("avatars/cat001.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String strPath = uri.toString();
                Toast.makeText(mContext, strPath,
                        Toast.LENGTH_SHORT).show();
                Glide.with(mContext).load(strPath).into(imageView);
            }
        });

 */


         // jesli chcesz, aby to dzialalo daj on successlistener :) ale nie działa :(
        //Glide.with(this).load(R.drawable.cat_movie).into(imageView);
        /*
        storageReference.child("avatars/cat001.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                loadImage(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        */

        Button logOutBtn = findViewById(R.id.logoutBtn);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainMenu.class);
                startActivity(intent);

            }
        });

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_account_acc);
    }

    private void loadImage(Uri downloadUrl) {
        Toast.makeText(this, "Loading",
                Toast.LENGTH_SHORT).show();
        Glide.with(this).load(downloadUrl.getPath()).into(imageView);
    }

}
