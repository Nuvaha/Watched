package com.example.watched.main;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watched.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, curent_state;

    private CircleImageView imProfile;
    private TextView tvUserName;
    private TextView tvStatus;
    private Button btnSendMessage;
    private Button btnCancelChatRequest;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat requests");

        //contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        curent_state = "new";

        //Toast.makeText(ProfileActivity.this, "User Id: " + receiverUserId, Toast.LENGTH_SHORT).show();

        init();

        retrieveUserInfo();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageChatrequests();
            }
        });
    }

    private void init() {
        imProfile = findViewById(R.id.visit_profile_image);
        tvUserName = findViewById(R.id.visit_profile_user_name);
        tvStatus = findViewById(R.id.visit_profile_status);
        btnSendMessage = findViewById(R.id.send_message_request_button);
        btnCancelChatRequest = findViewById(R.id.cancel_chat_request_button);
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(imProfile);
                    tvUserName.setText(userName);
                    tvStatus.setText(userStatus);

                }else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    tvUserName.setText(userName);
                    tvStatus.setText(userStatus);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatrequests() {
        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                curent_state = "request_sent";
                                btnSendMessage.setText("Cancel chat request");
                            }else if (request_type.equals("received")){
                                curent_state = "request_received";
                                btnSendMessage.setText("Accept chat request");
                                btnCancelChatRequest.setVisibility(View.VISIBLE);
                                btnCancelChatRequest.setEnabled(true);

                                btnCancelChatRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else {
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserId)){
                                                curent_state = "friends";
                                                btnSendMessage.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserId.equals(receiverUserId)){
            btnSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendMessage.setEnabled(false);
                    if (curent_state.equals("new")){
                        sendChatRequest();
                    }
                    if (curent_state.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if (curent_state.equals("request_received")){
                        acceptChatRequest();
                    }
                    if (curent_state.equals("friends")){
                        removespecificContact();
                    }
                }
            });

        }else {
            btnSendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", senderUserId);
                                                chatNotification.put("type", "request");

                                                notificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    btnSendMessage.setEnabled(true);
                                                                    curent_state = "request_sent";
                                                                    btnSendMessage.setText("Cancel chat request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }else {
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                btnSendMessage.setEnabled(true);
                                                curent_state = "new";
                                                btnSendMessage.setText("Send message");

                                                btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                                btnCancelChatRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            btnSendMessage.setEnabled(true);
                                            curent_state = "friends";
                                            btnSendMessage.setText("Remove this contact");

                                            btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                            btnCancelChatRequest.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void removespecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                btnSendMessage.setEnabled(true);
                                                curent_state = "new";
                                                btnSendMessage.setText("Send message");

                                                btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                                btnCancelChatRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}
