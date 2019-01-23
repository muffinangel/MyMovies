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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {


    private FirebaseUser user;
    private SearchView searchView;
    private ImageButton addBtn;
    private TextView promptForCamera;
    private ListView listView;
    private String userUID;

    private UserAdapter uAdapter ;
    private ArrayList<String> usersIDs = new ArrayList<>();;
    private ArrayList<User> usersList = new ArrayList<>(); ;
    private String query;
    private FirebaseFirestore db ;
    private Context mContext ;

    private User selected;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_search_sea:
                    updateUI(SearchActivity.class);
                    return true;
                case R.id.navigation_favorites_sea:
                    updateUI(FavoritesActivity.class);
                    return true;
                case R.id.navigation_account_sea:
                    updateUI(AccountActivity.class);
                    return true;
                case R.id.navigation_library_sea:
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Intent intent = getIntent();
        user = (FirebaseUser) intent.getExtras().getSerializable("CURRENT_USER");
        userUID = intent.getStringExtra("USER_ID");

        db = FirebaseFirestore.getInstance();
        mContext = this;
        uAdapter = new UserAdapter(this,usersList);
        searchView = findViewById(R.id.searchView);
        addBtn = findViewById(R.id.imageButton);
        addBtn.setVisibility(View.GONE);
        this.setTitle("Friends");
        listView = findViewById(R.id.friendsView);
        listView.setVisibility(View.VISIBLE);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //display users using some adapter
        db.collection("friends").document(userUID).collection(userUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        //moviesIDs.add(document.getId());
                        usersIDs.add(document.getId());
                        db.collection("users").document(document.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                usersList.add(new User( (String) documentSnapshot.get("nick"), (String) documentSnapshot.get("about"), (String) documentSnapshot.get("avatar_path"), documentSnapshot.getId(), true ) );
                                listView.setAdapter(uAdapter);
                                //Toast.makeText(mContext, "masz przyjaciol",
                                //Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    // Toast.makeText(LibraryActivity.this, "sa filmy",
                    // Toast.LENGTH_SHORT).show();
                    //getAllFilmsData(moviesIDs);
                }
                //taskIsRunning = false;
            }
        });


        //set listener na search fielda - wyświetl listę userów o tym nicku
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                setQuery(query);
                db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                String nick = (String) document.get("nick");
                                if(nick.startsWith(query) || query.startsWith(nick)) {
                                    User u = new User(nick, (String)document.get("about"), (String)document.get("avatar_path"), document.getId(), false);
                                    usersList.add(u);
                                    //Toast.makeText(mContext, "sa userzy",
                                    // Toast.LENGTH_SHORT).show();
                                }
                            }
                            listView.setAdapter(uAdapter);


                        }

                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // Toast.makeText(getApplicationContext(),"KLIK", Toast.LENGTH_SHORT).show();
                addBtn.setVisibility(View.GONE);
                listView.setSelection(position);
                // powinnismy przekazac wszystkie Movie Info i wywolac inny widok?
                //TODO - zmienić library tak, aby pobierała na wyświetlanie uid użytkownika - cos nie dziala :(
                //pobierajmy też czy friends zy nie friends
                User clicked = usersList.get(position);
                // TODO highlighting - sprawdz zakladki
                if(clicked.getUserFriend()) { //is user friend
                    // TODO - cos nie dziala - moze nie jest friendsem? :(
                    Log.d("FRIENDS", "klikniety jest friendsem usera " + clicked.getUid());
                    Intent intent= new Intent(mContext, LibraryActivity.class);
                    intent.putExtra("CURRENT_USER", user);
                    intent.putExtra("USER_ID", userUID);
                    intent.putExtra("FRIEND_UID", clicked.getUid());
                    startActivity(intent);
                }
                else {
                    Log.d("FRIENDS", "klikniety nie jest friendsem usera " + clicked.getUid());
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setSelection(position);
                    addBtn.setVisibility(View.VISIBLE);
                    selected = clicked;
                }

                // if friends
                // zobacz jego bibliotekę
                // if not -> dodaj jako friendsa?
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(selected != null) {
                    //1. dodaj do friends requests - jeśli jeszcze nie dodany (nawet jeśli, to sie chyba nadpisze)

                    Map<String, Object> data = new HashMap<>();
                    data.put("nick", selected.getNick());
                    data.put("description", selected.getAbout());
                    db.collection("friends_requests").document(selected.getUid()).collection(selected.getUid()).document(userUID).set(data);

                    //2. usun z listy -
                    usersList.remove(selected);
                    selected = null;
                    addBtn.setVisibility(View.GONE);

                    //3. wyswietl toasta
                    Toast.makeText(mContext,"send friends requests", Toast.LENGTH_SHORT).show();

                    //4. wyswietl nowa liste
                    listView.setAdapter(uAdapter);

                }
            }
        });


        // może zamiast image button zobacz requesty - kliknięcie oznacza dodanie użytkownika do frinedsów


        //set listener na element listy - jeśli kliknie, przejdź do biblioteki friendsa -> wymaga to modyfikacji libraryActivity

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //navigation.setSelectedItemId(R.id.navigation_friends_lib); // doesnt work?
        navigation.setSelectedItemId(R.id.navigation_friends_sea);
    }

    private void setQuery(String query) {
        this.query = query;
    }


}
