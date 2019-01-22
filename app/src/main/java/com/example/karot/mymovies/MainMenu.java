package com.example.karot.mymovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;

import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextMessage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button signinBtn;
    private Button signupBtn;
    private static final String TAG = "MainMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mAuth = FirebaseAuth.getInstance();
        signinBtn = (Button) findViewById(R.id.signinBtn);
        signupBtn = (Button) findViewById(R.id.signupBtn);
        signinBtn.setOnClickListener(this);
        signupBtn.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        //if user is logged in we should go to the next activity!
    }

    private void logIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainMenu.this, "Authentication sucseded.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user, LibraryActivity.class);
                            //wykomentuj toast i zrob intent do innej aktywnosci

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainMenu.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user, Class cls) {
        Intent intent= new Intent(this, cls);
        intent.putExtra("CURRENT_USER", user); //cant do that ?!
        intent.putExtra("USER_ID", user.getUid());
        intent.putExtra("USER_EMAIL", user.getEmail());
        //intent.putExtra("CUREENT_AUTH", mAuth);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.signinBtn: {
                EditText username = (EditText)findViewById(R.id.emailField);
                EditText password = (EditText)findViewById(R.id.passwordField);
                this.logIn(username.getText().toString().trim(), password.getText().toString().trim());
                break;
            }

            case R.id.signupBtn: {
                // do something for button 2 click
                // dodaj do userów
                // stwórz mu library!!!
                EditText username = (EditText)findViewById(R.id.emailField);
                EditText password1 = (EditText)findViewById(R.id.passwordField);

                User added = new User(username.getText().toString(), "");
                //  doesnt work:
                //final FirebaseDatabase database = FirebaseDatabase.getInstance();
                //DatabaseReference ref = database.getReference("mymovies-91b8f");
                //DatabaseReference uniqueKey = ref.push();

                String email = username.getText().toString();
                String password = password1.getText().toString();

                if (TextUtils.isEmpty(email)) {
                            Toast.makeText(getApplicationContext(), "Enter Eamil Id", Toast.LENGTH_SHORT).show();
                            return;
                }
                if (TextUtils.isEmpty(password)) {
                            Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                            return;
                }
                        //progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainMenu.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        //progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "createUserWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            updateUI(user, RegisterActivity.class);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(MainMenu.this, "Registration failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                break;
            }

            //.... etc
        }
    }
}
