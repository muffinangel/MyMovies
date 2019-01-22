package com.example.karot.mymovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private String email;
    private Button okBtn;
    private String userUID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context mContext = this;
    EditText nick;
    EditText about;

    private void addUserToUsers() {
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> userNew = new HashMap<>();
        userNew.put("about", "I prefer to remain a mystery");
        userNew.put("avatar_path", "cat001.jpeg");
        userNew.put("nick", email);

        db.collection("users").document(userUID)
                .set(userNew)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Register", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Register", "Error writing document", e);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        userUID = intent.getStringExtra("USER_ID");
        email = intent.getStringExtra("USER_EMAIL");
        addUserToUsers();

        okBtn = findViewById(R.id.OKbutton);
        nick = findViewById(R.id.nick);
        about = findViewById(R.id.about);

        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String nickStr = nick.getText().toString();
                String aboutStr = about.getText().toString();

                if(nickStr != null && nickStr.length() > 2 && aboutStr != null && aboutStr.length() > 2) {
                    //update fields from
                    db.collection("users").document(userUID).
                            update("nick", nickStr, "about", aboutStr)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("REGISTER", "DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("REGISTER", "Error updating document", e);
                                }});
                }
                else if(nickStr != null && nickStr.length() > 2) {
                    db.collection("users").document(userUID).
                            update("nick", nickStr)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("REGISTER", "DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("REGISTER", "Error updating document", e);
                                }});
                }
                else if( aboutStr != null && aboutStr.length() > 2) {
                    db.collection("users").document(userUID).
                            update("about", aboutStr)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("REGISTER", "DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("REGISTER", "Error updating document", e);
                                }});
                }

                // Perform action on click
                Intent intent = new Intent(mContext, LibraryActivity.class);
                intent.putExtra("USER_ID", userUID);
                startActivity(intent);
            }
        });
    }

}
