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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

public class LibraryActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseUser user;
    private String userUID;
    private String friendUID;
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
                    updateUI(FavoritesActivity.class);
                    return true;
                case R.id.navigation_account_lib:
                    if (taskIsRunning == false) {
                        mTextMessage.setText(getString(R.string.navigation_account_str));
                        updateUI(AccountActivity.class);
                    }
                    return true;
                case R.id.navigation_search_lib:
                    if (taskIsRunning == false) {
                        mTextMessage.setText(getString(R.string.navigation_search_str));
                        updateUI(SearchActivity.class);
                    }
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

    private void addUserToUsers() {
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> userNew = new HashMap<>();
        userNew.put("about", "");
        userNew.put("avatar_path", "cat001.jpeg");
        userNew.put("nick", email);

        db.collection("users").document(userUID)
                .set(userNew)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void getAllFilmsData(ArrayList<String> moviesIDs) {
        Toast.makeText(LibraryActivity.this, "s: " + (moviesIDs.size()),
                Toast.LENGTH_SHORT).show();
        for(String id: moviesIDs) {
            db.collection("movies_db").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Movie m = new Movie((String) documentSnapshot.get("poster_link"), (String)documentSnapshot.get("title"), (String)documentSnapshot.get("release"), (String)documentSnapshot.get("rating"),
                            (String)documentSnapshot.get("genre"), (String)documentSnapshot.get("duration"), (String)documentSnapshot.get("description"), documentSnapshot.getId());
                    movies.add(m);
                   // imageAdapter.addMovie(m);
                    gridview.setAdapter(imageAdapter);
                    Toast.makeText(LibraryActivity.this, "success ",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        Toast.makeText(LibraryActivity.this, "z: " + (movies.size()),
                Toast.LENGTH_SHORT).show(); // 0 !
        gridview.setAdapter(imageAdapter);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTitle("Library");
        taskIsRunning = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        gridview = (GridView) findViewById(R.id.gridView);
        mTextMessage = findViewById(R.id.message);
        mTextMessage.setText(getString(R.string.test0001));

        Intent intent = getIntent();
        user = (FirebaseUser) intent.getExtras().getSerializable("CURRENT_USER");
        userUID = intent.getStringExtra("USER_ID");
        friendUID = intent.getStringExtra("FRIEND_UID");
        email = intent.getStringExtra("USER_EMAIL");


        // Create a new ThreadPoolExecutor with 2 threads for each processor on the
// device and a 60 second keep-alive time.
        int numCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores *2,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());


       // db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        CollectionReference libRef = db.collection("libraries");
// Create a query against the collection.

        //myRef.setValue("Hello, World!");
        //sprawdz, czy jest user - jesli nie, dodaj go
        String usingID = userUID;
        if(friendUID != null && !friendUID.equals("")) {
            usingID = friendUID;
            Log.d(TAG, "PRZESZLISMY Z FRIENDSOW!!!");
        }


        // TODO to rob tylko raz!

        /*
        DocumentReference userExistQuery = usersRef.document(userUID);
        taskIsRunning = true;
        userExistQuery.get().addOnSuccessListener(executor, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               // Toast.makeText(LibraryActivity.this, "jest user???",
                //        Toast.LENGTH_SHORT).show();
                if(documentSnapshot.getData() == null) {
                    addUserToUsers();
                }
                else {
                   // Toast.makeText(LibraryActivity.this, "JEST",
                          //  Toast.LENGTH_SHORT).show();
                }
                taskIsRunning = false;
            }
        });
        */
        taskIsRunning = true;


        //sprawdz, czy jest library usera - wyświetl wszystkie postery filmow na ekranie glownym
        CollectionReference libExistQuery = libRef.document(usingID).collection(usingID);
        //libExistQuery.
        libExistQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        moviesIDs.add(document.getId());
                    }
                   // Toast.makeText(LibraryActivity.this, "sa filmy",
                           // Toast.LENGTH_SHORT).show();
                    getAllFilmsData(moviesIDs);



                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                taskIsRunning = false;
            }
        });



                    //dla każdego filmu:
                    // 1. pobierz informacje
                    // 2. wyświetl postera
                    // 3. nastaw listenera na poster
                    // 4. jesli klikniecie - przekaz do innego widoku i wyswietl film
                   // ArrayList<String> a = data.values();



        // GridView

        //czy czekać na threada, aby się skończył?

       // executor.shutdown();
       // try {
       //     executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      //  } catch (InterruptedException e) {
      //  }
// -> wywołuje internal error w firestorze :(



        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    Toast.makeText(LibraryActivity.this, "" + position,
                            Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "hahaha", Toast.LENGTH_SHORT).show();



        //BottomNavigationView navigation = findViewById(R.id.navigation_library);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId( R.id.navigation_library_lib);
    }

}

/* original grid

    <GridView
        android:id="@+id/gridView"
        android:layout_width="368dp"

        android:layout_height="438dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        android:layout_marginRight="8dp"
        android:layout_marginBottom="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="1.0"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.0" />
 */
