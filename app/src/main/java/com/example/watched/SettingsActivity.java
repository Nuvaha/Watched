package com.example.watched;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView imgProfile;
    private EditText edtUsername, edtStatus;
    private Button btnUpdate;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();

        //edtUsername.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSetting();
            }
        });
        retrieveUserInfo();
    }

    private void init() {
        imgProfile = findViewById(R.id.profile_image);
        edtUsername = findViewById(R.id.edit_profile);
        edtStatus = findViewById(R.id.edit_status);
        btnUpdate = findViewById(R.id.btn_update);

    }

    private void updateSetting() {
        String username = edtUsername.getText().toString();
        String status = edtStatus.getText().toString();
        if (TextUtils.isEmpty(username)){
            Toast.makeText(SettingsActivity.this, "please write your user name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)){
            Toast.makeText(SettingsActivity.this, "please write your status...", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String>profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", username);
            profileMap.put("status", status);
            rootRef.child("User").child(currentUserId).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "frofile updated successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserInfo() {
        rootRef.child("User").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    edtUsername.setText(retrieveUserName);
                    edtStatus.setText(retrieveStatus);



                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    edtUsername.setText(retrieveUserName);
                    edtStatus.setText(retrieveStatus);

                }else {
                    //edtUsername.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "please set & update your", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        //finish();
    }
}
