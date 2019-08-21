package com.example.watched.main;

import android.app.ProgressDialog;
import android.app.usage.StorageStats;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watched.fragment.ChatsFragment;
import com.example.watched.model.Messages;
import com.example.watched.adapter.MessagesAdapter;
import com.example.watched.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderId;

    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar chatToolbar;

    private EditText edtInputMessage;
    private ImageButton btnSendMessage;

    private ImageButton btnSendFile;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessageList;

    private String saveCurrentTime, saveCurrentDate;

    private String checker = " ", myUrl = " ";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle bundle = getIntent().getExtras();

        messageReceiverId = bundle.getString("visit_user_id");
        messageReceiverName = bundle.getString("visit_user_name");
        messageReceiverImage = bundle.getString("visit_image");




        //Toast.makeText(ChatActivity.this, messageReceiverId, Toast.LENGTH_SHORT).show();
        //Toast.makeText(ChatActivity.this, messageReceiverName, Toast.LENGTH_SHORT).show();
        init();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loadingbar = new ProgressDialog(this);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seveMessage();
            }

        });

        displayLastSeen();

        btnSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "Ms Word Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select image"),438);
                        }
                        if (i == 1){
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select pdf file"), 438);
                        }
                        if (i == 2){
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select ms word file"), 438);
                        }
                    }
                });
                builder.show();
            }

        });


    }

    private void init() {

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewActionBar = inflater.inflate(R.layout.custom_chat_bar, chatToolbar);

        //getSupportActionBar().setCustomView(viewActionBar);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        edtInputMessage = findViewById(R.id.edt_input_message);
        btnSendMessage = findViewById(R.id.btn_send_message);

        btnSendFile = findViewById(R.id.send_files_btn);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.private_chat);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDateFormat.format(calendar.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTimeFormat.format(calendar.getTime());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null ){
            final Handler handler = new Handler(){
                public void handleMessage(Message msg){
                    super.handleMessage(msg);
                    loadingbar.incrementProgressBy(4);
                }
            };
            messagesList.clear();
            loadingbar.setTitle("Sending file");
            //loadingbar.setMessage("Please wait,we are sending that file... ");
            loadingbar.setMax(100);
            loadingbar.setMessage("Loading...");
            loadingbar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();
            loadingbar.setCancelable(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (loadingbar.getProgress() <= loadingbar.getMax()){
                            Thread.sleep(200);
                            handler.sendMessage(handler.obtainMessage());
                            if (loadingbar.getProgress() == loadingbar.getMax()){
                                loadingbar.dismiss();
                            }
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }).start();

            fileUri = data.getData();

            if (!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderId).child(messageReceiverId).push();

                final  String messagePushId = userMessageKeyRef.getKey();

                final  StorageReference filePath = storageReference.child(messagePushId + "." + checker);
                uploadTask = filePath.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                Map messageTextBody = new HashMap();

                                messageTextBody.put("message" ,url );
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderId);
                                messageTextBody.put("to", messageReceiverId);
                                messageTextBody.put("messageId", messagePushId);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushId,messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushId,messageTextBody);

                                rootRef.updateChildren(messageBodyDetails);
                                loadingbar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingbar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } );
            }
            else if (checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderId).child(messageReceiverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + ".jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){

                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to", messageReceiverId);
                            messageTextBody.put("messageId", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId,messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "file sent successfully...", Toast.LENGTH_SHORT).show();
                                    }else {
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    edtInputMessage.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else {
                loadingbar.dismiss();
                Toast.makeText(ChatActivity.this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void displayLastSeen(){
        rootRef.child("User").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")){
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){

                                userLastSeen.setText("Online Now");

                            }else if (state.equals("offline")){

                                userLastSeen.setText("Last seen: " + date + "  " + time);
                            }
                            }else {
                                userLastSeen.setText("Offline");
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    messagesList.add(postSnapshot.getValue(Messages.class));
                }
                messagesAdapter.notifyDataSetChanged();
                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seveMessage() {
        final String messageText = edtInputMessage.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this, "first write your message...", Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageId", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        loadingbar.dismiss();
                        Toast.makeText(ChatActivity.this, "message sent successfully...", Toast.LENGTH_SHORT).show();
                    }else {
                        loadingbar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    edtInputMessage.setText("");
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
