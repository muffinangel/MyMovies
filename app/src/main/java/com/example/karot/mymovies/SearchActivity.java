package com.example.karot.mymovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseUser user;
    private SearchView searchView;
    private CheckBox checkBoxMyLib;
    private CheckBox checkBoxFriendsLib;
    private CheckBox checkBoxIMDB;
    private ListView listView;
    private String userUID;


    private ArrayList<Movie> moviesList ;
    private ArrayList<Movie> moviesExistingOnList ;
    private ArrayList<String> moviesIDs = new ArrayList<>();
    private MovieAdapter mAdapter ;
    private String query_;
    private FirebaseFirestore db ;
    private Context mContext ;

    //może obiekt z danymi do wrzucenia do bazy? :) -> dodatkowa klasa?

    // checkBoxIMDB ALBO (checkBoxMyLib or checkBoxFriendsLib)

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends_sea:
                    mTextMessage.setText(getString(R.string.navigation_friends_str));
                    updateUI(FriendsActivity.class);
                    return true;
                case R.id.navigation_favorites_sea:
                    mTextMessage.setText(getString(R.string.navigation_favorites_str));
                    updateUI(FavoritesActivity.class);
                    return true;
                case R.id.navigation_account_sea:
                    mTextMessage.setText(getString(R.string.navigation_account_str));
                    updateUI(AccountActivity.class);
                    return true;
                case R.id.navigation_search_sea:
                    mTextMessage.setText(getString(R.string.navigation_search_str));
                    return true;
                case R.id.navigation_library_sea:
                    mTextMessage.setText(getString(R.string.navigation_library_str));
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
                    Movie m = new Movie((String) documentSnapshot.get("poster_link"), (String)documentSnapshot.get("title"), (String)documentSnapshot.get("release"), (String)documentSnapshot.get("imdb_rating"),
                            (String)documentSnapshot.get("genre"), (String)documentSnapshot.get("duration"), (String)documentSnapshot.get("description"), documentSnapshot.getId());
                    String title = (String)documentSnapshot.get("title");
                    if((title.toUpperCase()).startsWith((query_.toUpperCase())) || (query_.toUpperCase()).startsWith((title.toUpperCase())))
                        moviesList.add(m);
                    // imageAdapter.addMovie(m);
                    listView.setAdapter(mAdapter);
                }
            });
        }
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        moviesList = new ArrayList<>();
        moviesExistingOnList = new ArrayList<>();
        mAdapter = new MovieAdapter(this,moviesList);;
        db = FirebaseFirestore.getInstance();
        mContext = this;
        this.setTitle("Search");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        user = (FirebaseUser) intent.getExtras().getSerializable("CURRENT_USER");
        userUID = intent.getStringExtra("USER_ID");

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_search_sea);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_search_sea);


        searchView = findViewById(R.id.search);
        checkBoxMyLib = findViewById(R.id.checkBoxMyLib);
        checkBoxFriendsLib = findViewById(R.id.checkBoxFriendsLib);
        checkBoxIMDB = findViewById(R.id.checkBoxIMDB);
        listView = findViewById(R.id.friendsView);
        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // powinnismy przekazac wszystkie Movie Info i wywolac inny widok?
                oneFilmSelected(moviesList.get(position));
            }
        });







        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//              if (searchView.isExpanded() && TextUtils.isEmpty(newText)) {
               // callSearch(newText); // może wykomentować, żeby szukało na koniec nam
//              }
                return true;
            }

            public void callSearch(String query) {
                //Do searching
              //  moviesList = new ArrayList<>(); <- chyba psuje
                moviesList.clear();
                moviesIDs.clear();
                listView.requestLayout();
                doSearch(query);
            }

        });
    }


    private void doSearch(String query) {
        checkBoxIMDB = findViewById(R.id.checkBoxIMDB);
        query_ = query;

        if(checkBoxIMDB != null && checkBoxIMDB.isChecked()) { // szukamy w IMDB
            //   http://www.omdbapi.com/?apikey=1a4e6d45&t=Star+Wars
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://www.omdbapi.com/?apikey=1a4e6d45&t=" + preproccessQuery(query);
            JsonObjectRequest request = new JsonObjectRequest(url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (null != response) {
                                try {
                                    //handle your response
                                    String title = response.getString("Title");
                                    String year = response.getString("Year");
                                    String posterLink = response.getString("Poster");
                                    JSONArray imdbRating = response.getJSONArray("Ratings");
                                    JSONObject imdbRating_info = imdbRating.getJSONObject(0);
                                    String rating = imdbRating_info.getString("Value");
                                    // "Ratings":[{"Source":"Internet Movie Database","Value":"8.6/10"},{"Source":"Rotten Tomatoes","Value":"93%"}
                                    String genre = response.getString("Genre");
                                    String duration = response.getString("Runtime");
                                    String description = response.getString("Plot");
                                    String id = response.getString("imdbID");

                                    displayInfo(title, year, posterLink, rating, genre, duration, description, id);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(request);
        }
        else if ( (checkBoxMyLib != null && checkBoxMyLib.isChecked()) || ( checkBoxFriendsLib != null &&  checkBoxFriendsLib.isChecked())){
            //w my lib
                //sprawdz, czy we friends też
            listView = (ListView) findViewById(R.id.friendsView);
            moviesList = new ArrayList<>();
            mAdapter = new MovieAdapter(this,moviesList);

    if (checkBoxMyLib != null && checkBoxMyLib.isChecked()) {
        db.collection("libraries").document(userUID).collection(userUID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Search", document.getId() + " => " + document.getData());
                        moviesIDs.add(document.getId());
                    }
                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                    // Toast.LENGTH_SHORT).show();
                    getAllFilmsData(moviesIDs);


                    if ( checkBoxFriendsLib != null &&  checkBoxFriendsLib.isChecked()) {
                        db.collection("friends").document(userUID).collection(userUID)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("Search", document.getId() + " => " + document.getData());
                                        //document ID jest id-kiem friendsa
                                        db.collection("libraries").document(document.getId()).collection(document.getId())
                                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d("Search", document.getId() + " => " + document.getData());
                                                        if(moviesIDs.contains(document.getId()) == false)
                                                            moviesIDs.add(document.getId());
                                                    }
                                                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                                                    // Toast.LENGTH_SHORT).show();
                                                    getAllFilmsData(moviesIDs);

                                                } else {
                                                    Log.d("Search", "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });

                                    }
                                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                                    // Toast.LENGTH_SHORT).show();
                                    getAllFilmsData(moviesIDs);

                                } else {
                                    Log.d("Search", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }



                } else {
                    Log.d("Search", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    if ( checkBoxFriendsLib != null &&  checkBoxFriendsLib.isChecked()) {
        db.collection("friends").document(userUID).collection(userUID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Search", document.getId() + " => " + document.getData());
                        //document ID jest id-kiem friendsa
                        db.collection("libraries").document(document.getId()).collection(document.getId())
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("Search", document.getId() + " => " + document.getData());
                                        if(moviesIDs.contains(document.getId()) == false)
                                            moviesIDs.add(document.getId());
                                    }
                                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                                    // Toast.LENGTH_SHORT).show();
                                    getAllFilmsData(moviesIDs);

                                } else {
                                    Log.d("Search", "Error getting documents: ", task.getException());
                                }
                            }
                        });

                    }
                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                    // Toast.LENGTH_SHORT).show();
                    getAllFilmsData(moviesIDs);

                } else {
                    Log.d("Search", "Error getting documents: ", task.getException());
                }
            }
        });



        listView.requestFocus();
    }














            // najpierw w ogóle znajdź te filmy o tym tutule w movies_db: (chyba powinniśmy przelecieć przez wszystkie)
            // TODO naprawić
/*
            db.collection("movies_db").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (final QueryDocumentSnapshot d : task.getResult()) {
                            Movie m = new Movie((String) d.get("poster_link"),
                                    (String) d.get("title"),
                                    (String) d.get("release"),
                                    (String) d.get("imdb_rating"),
                                    (String) d.get("genre"),
                                    (String) d.get("duration"),
                                    (String) d.get("description"),
                                    d.getId());
                            moviesExistingOnList.add(m);
                            String title = (String) d.get("title");


                            /*
                            if (title.startsWith(query_) || query_.startsWith(title)) {
                                // tu sprawdz czy w swojej jest klikniete
                                if ((checkBoxMyLib != null && checkBoxMyLib.isChecked())) {
                                    db.collection("libraries").document(userUID).collection(userUID).document(d.getId()).
                                            get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.getData() != null) {
                                                for (Movie a : moviesExistingOnList) {
                                                    if (a.getId().equals(documentSnapshot.getId())) {
                                                        moviesList.add(a);
                                                        listView.setAdapter(mAdapter);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }

                                if ((checkBoxFriendsLib != null && checkBoxFriendsLib.isChecked())) {
                                    db.collection("friends").document(userUID).collection(userUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (final QueryDocumentSnapshot d : task.getResult()) {
                                                String friend_uid = d.getId();
                                                // wywolaj funkcje, ktora bedzie wyszukiwała?

                                            }
                                        }
                                    });
                                }
                            }
                        }

                    }
                }
            }); */

/*


            db.collection("movies_db").whereEqualTo(query, "title").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    // TODO jakos inaczej trzeba robic te query snapshoty
                    if (e != null) {
                       // Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    //if snapshot == null or
                    if(snapshot == null || snapshot.isEmpty()) {
                        //włącz widok postera i wyślij toasta, że nie ma
                        cameraBtn.setVisibility(View.VISIBLE);
                        promptForCamera.setVisibility(View.VISIBLE);
                        Toast.makeText(mContext, "No film with such title", // OJOJ
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(mContext, "istnieje w movies_db",
                                Toast.LENGTH_SHORT).show();
                        for(DocumentSnapshot d: snapshot) { //movies w movies_db
                            Movie m = new Movie((String) d.get("poster_link"), (String) d.get("title") , (String)d.get("release"), (String)d.get("imdb_rating"),
                                    (String)d.get("genre"), (String)d.get("duration"), (String) d.get("description"),  d.getId());
                            moviesExistingOnList.add(m);


                            // jeżeli to w swojej bibliotece
                            db.collection("libraries").document(userUID).collection(userUID).document(d.getId()).
                                    get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.getData() != null) {
                                        Toast.makeText(mContext, "istnieje w library_usera",
                                                Toast.LENGTH_SHORT).show();
                                        for(Movie a: moviesExistingOnList) {
                                            if(a.getId() == documentSnapshot.getId()) {
                                                moviesList.add(a);

                                                .setAdapter(mAdapter);
                                                return;
                                            }
                                        }
                                    }
                                }
                            });
                            //displayInfo((String) d.get("title"), (String)d.get("release"), (String) d.get("poster_link"), (String)d.get("imdb_rating"),
                            //        (String)d.get("genre"), (String)d.get("duration"),(String) d.get("description"), d.getId());

                            // TODO w bibliotekach innych!!
                        }
                    }

                    //if (snapshot != null && snapshot.exists()) {
                       // Log.d(TAG, "Current data: " + snapshot.getData());
                        ;
                    //} else {
                        //Log.d(TAG, "Current data: null");
                        ;
                    //}
                }
            });




            //dodaj elementy
            if (checkBoxMyLib != null && checkBoxMyLib.isChecked()) {

            }


            listView.setAdapter(mAdapter); */
        }

    }

    private void oneFilmSelected(Movie m) {
        Intent intent= new Intent(this, MovieActivity.class);
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

    private void displayInfo(String title, String year, String poster, String rating, String genre, String duration, String description, String id) { //trzeba dodać pzoostałe informacje, aby po kliknięciu móc wrzucić do własnej biblioteki
        Toast.makeText(this, title,
                Toast.LENGTH_SHORT).show();
        listView = (ListView) findViewById(R.id.friendsView);
        moviesList.add(new Movie(poster, title , year, rating, genre, duration, description, id));

        listView.setAdapter(mAdapter);


    }

    private String preproccessQuery(String query) {
        return query.replace(' ', '+');
    }

}
