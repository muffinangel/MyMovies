package com.example.karot.mymovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FavoritesActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseUser user;
    private String userUID;
    private String email;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private FirebaseAuth mAuth;
    private static final String TAG = "LibraryActivity";
    private Boolean taskIsRunning;
    private GridView gridview;

    private ArrayList<String> moviesIDs = new ArrayList<>();
    private ArrayList<Movie> movies = new ArrayList<>();
    private Context mContext = this;
    private ImageAdapter imageAdapter = new ImageAdapter(this, movies);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends_lib:
                    mTextMessage.setText(getString(R.string.navigation_friends_str));
                    updateUI(FriendsActivity.class);
                    return true;
                case R.id.navigation_favorites_lib:
                    mTextMessage.setText(getString(R.string.navigation_favorites_str));
                    return true;
                case R.id.navigation_account_lib:
                        mTextMessage.setText(getString(R.string.navigation_account_str));
                        updateUI(AccountActivity.class);
                    return true;
                case R.id.navigation_search_lib:
                        mTextMessage.setText(getString(R.string.navigation_search_str));
                        updateUI(SearchActivity.class);
                    return true;
                case R.id.navigation_library_lib:
                    updateUI(LibraryActivity.class);
                    return true;

            }
            return false;
        }
    };

    private void updateUI(Class cls) {
        Intent intent= new Intent(this, cls);
        intent.putExtra("CURRENT_USER", user);
        intent.putExtra("USER_ID", userUID);
        //intent.putExtra("CUREENT_AUTH", mAuth);
        startActivity(intent);
    }


    private void getAllFilmsData(ArrayList<String> moviesIDs) {

        for(String id: moviesIDs) {
            db.collection("movies_db").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Movie m = new Movie((String) documentSnapshot.get("poster_link"), (String)documentSnapshot.get("title"), (String)documentSnapshot.get("release"), (String)documentSnapshot.get("rating"),
                            (String)documentSnapshot.get("genre"), (String)documentSnapshot.get("duration"), (String)documentSnapshot.get("description"), documentSnapshot.getId());
                    movies.add(m);
                    // imageAdapter.addMovie(m);
                    gridview.setAdapter(imageAdapter);

                }
            });
        }

        gridview.setAdapter(imageAdapter);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTitle("Favorites");
        taskIsRunning = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        gridview = (GridView) findViewById(R.id.gridView);
        mTextMessage = findViewById(R.id.message);
        mTextMessage.setText(getString(R.string.test0001));

        Intent intent = getIntent();
        user = (FirebaseUser) intent.getExtras().getSerializable("CURRENT_USER");
        userUID = intent.getStringExtra("USER_ID");

        CollectionReference libRef = db.collection("libraries");

        //sprawdz, czy jest library usera - wyświetl wszystkie postery filmow na ekranie glownym
        CollectionReference libExistQuery = libRef.document(userUID).collection(userUID);
        //libExistQuery.
        libExistQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Boolean fav = (Boolean) document.get("favorite");
                        if(fav == true)
                            moviesIDs.add(document.getId());
                    }
                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                    // Toast.LENGTH_SHORT).show();
                    getAllFilmsData(moviesIDs);



                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });




        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Movie m = movies.get(position);
                Intent intent= new Intent(mContext, MovieActivity.class);
                intent.putExtra("CURRENT_USER", user);
                intent.putExtra("USER_ID", userUID);

                intent.putExtra("M_TITLE", m.getmName());
                intent.putExtra("M_DESCRIPTION", m.getDescription());
                intent.putExtra("M_DURATION", m.getDuration());
                intent.putExtra("M_GENRE", m.getGenre());
                intent.putExtra("M_ID", m.getId());
                intent.putExtra("M_POSTER", m.getmImageDrawable());
                intent.putExtra("M_RELEASE", m.getmRelease());
                intent.putExtra("M_RATING", m.getRating());

                startActivity(intent);
            }
        });
       // Toast.makeText(this, "hahaha", Toast.LENGTH_SHORT).show();



        //BottomNavigationView navigation = findViewById(R.id.navigation_library);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId( R.id.navigation_favorites_lib);
    }

}
