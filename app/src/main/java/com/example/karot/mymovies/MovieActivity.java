package com.example.karot.mymovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MovieActivity extends AppCompatActivity implements View.OnClickListener {

    private String userUID;
    private FirebaseUser user;

    private TextView ratingDescr;
    private TextView releaseDescr;
    private TextView addedDescr;
    private TextView durationDescr;
    private TextView descriptionDescr;
    private TextView lentDescr;
    private ImageView poster;

    private ImageButton trashBtn;
    private ImageButton addToFavBtn;
    private ImageButton addToLibBtn;
    private ImageView favImage;

    private Movie m;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Timestamp addedTime;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends_sea:
                    updateUI(FriendsActivity.class);
                    return true;
                case R.id.navigation_favorites_sea:
                    updateUI(FavoritesActivity.class);
                    return true;
                case R.id.navigation_account_sea:
                    updateUI(AccountActivity.class);
                    return true;
                case R.id.navigation_search_sea:
                    updateUI(SearchActivity.class);
                    return true;
                case R.id.navigation_library_sea:
                    updateUI(LibraryActivity.class);
                    return true;
            }
            return false;
        }
    };

    private void updateUI(Class cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("CURRENT_USER", user);
        intent.putExtra("USER_ID", userUID);
        //intent.putExtra("CUREENT_AUTH", mAuth);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Intent intent = getIntent();
        user = (FirebaseUser) intent.getExtras().getSerializable("CURRENT_USER");
        userUID = intent.getStringExtra("USER_ID");
        String title = intent.getStringExtra("M_TITLE");
        String description = intent.getStringExtra("M_DESCRIPTION");
        String duration = intent.getStringExtra("M_DURATION");
        String genre = intent.getStringExtra("M_GENRE");
        String posterPath = intent.getStringExtra("M_POSTER");
        String release = intent.getStringExtra("M_RELEASE");
        String rating = intent.getStringExtra("M_RATING");
        String id = intent.getStringExtra("M_ID");
        m = new Movie(posterPath, title, release, rating, genre, duration, description, id);


        ratingDescr = findViewById(R.id.rating);
        releaseDescr = findViewById(R.id.release);
        addedDescr = findViewById(R.id.added_to_library);
        durationDescr = findViewById(R.id.duration);
        descriptionDescr = findViewById(R.id.description);
        poster = findViewById(R.id.poster);

        trashBtn = findViewById(R.id.trashBtn);
        addToFavBtn = findViewById(R.id.addToFavBtn);
        addToLibBtn = findViewById(R.id.addNewBtn);
        favImage = findViewById(R.id.star);

        trashBtn.setOnClickListener(this);
        addToFavBtn.setOnClickListener(this);
        addToLibBtn.setOnClickListener(this);

        descriptionDescr.setText("Description: " + description);
        releaseDescr.setText("Release: " + release);
        durationDescr.setText("Duration: " + duration);
        ratingDescr.setText("Rating: " + rating);
        this.setTitle(title);

        Glide.with(this).load(posterPath).into(poster);

        // sprawdz, czy jest ten film w bibliotece i czy jest w fav
        // w bibliotece i nie w fav -> wyświetl trashBtn i addToFavBtn
        // w bibliotece i w Fav -> wyświetl trashBtn i favImage
        // nie w bilbiotece -> wyświetl addToLibBtn

        db = FirebaseFirestore.getInstance();
        DocumentReference movies = db.collection("libraries").document(userUID).collection(userUID).document(id);
        // DocumentReference userExistQuery = usersRef.document(userUID);

        movies.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toast.makeText(MovieActivity.this, "",
                        Toast.LENGTH_SHORT).show();
                Map<String, Object> data = documentSnapshot.getData();
                if (data == null) {
                    //addUserToUsers();
                    //Na PEwno nie ma w bibliotece
                    addToLibBtn.setVisibility(View.VISIBLE);
                    trashBtn.setVisibility(View.GONE);
                    addToFavBtn.setVisibility(View.GONE);
                    favImage.setVisibility(View.GONE);
                } else {
                    // Na Pewno jest w bibliotece
                    //1. pobrać added i czy fav
                    Object time = data.get("added_date");
                    Boolean fav = (Boolean) data.get("favorite");
                    if (fav == true) {
                        favImage.setVisibility(View.VISIBLE);
                        addToFavBtn.setVisibility(View.GONE);
                    } else {
                        favImage.setVisibility(View.GONE);
                        addToFavBtn.setVisibility(View.VISIBLE);
                    }
                    addToLibBtn.setVisibility(View.GONE);
                    trashBtn.setVisibility(View.VISIBLE);
                    addedDescr.setText("Added to library: " + time.toString());
                    //addedTime = time;
                }

            }
        });


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trashBtn: {
                db.collection("libraries").document(userUID).collection(userUID).document(m.getId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                updateUI(SearchActivity.class);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                break;
            }

            case R.id.addToFavBtn: {
                Boolean fav = true;
                Map<String, Object> data = new HashMap<>();
                data.put("favorite", fav);
                db.collection("libraries").document(userUID).collection(userUID).document(m.getId())
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                addToLibBtn.setVisibility(View.GONE);
                                trashBtn.setVisibility(View.VISIBLE);
                                addToFavBtn.setVisibility(View.GONE);
                                favImage.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                break;
            }

            case R.id.addNewBtn: {
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMAN);
                Timestamp sdf = new  Timestamp(new Date());
                Boolean fav = false;
                Map<String, Object> data = new HashMap<>();
                data.put("favorite", fav);
                data.put("added_date", sdf);

                Map<String, Object> movieData = new HashMap<>();
                movieData.put("description", m.getDescription());
                movieData.put("duration", m.getDuration());
                movieData.put("genre", m.getGenre());
                movieData.put("imdb_rating", m.getRating());
                movieData.put("poster_link", m.getmImageDrawable());
                movieData.put("release", m.getmRelease());
                movieData.put("title", m.getmName());

                db.collection("movies_db").document(m.getId())
                        .set(movieData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MovieActivity.this, "dodany do movies_db",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                db.collection("libraries").document(userUID).collection(userUID).document(m.getId())
                        .set(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                addToLibBtn.setVisibility(View.GONE);
                                trashBtn.setVisibility(View.VISIBLE);
                                addToFavBtn.setVisibility(View.VISIBLE);
                                favImage.setVisibility(View.GONE);
                                Toast.makeText(MovieActivity.this, "do library uzytkownika",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                break;
            }
        }
    }
};