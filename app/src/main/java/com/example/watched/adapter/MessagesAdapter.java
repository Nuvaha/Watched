package com.example.watched.adapter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watched.R;
import com.example.watched.main.ChatActivity;
import com.example.watched.main.ImageViewerActivity;
import com.example.watched.main.MainActivity;
import com.example.watched.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessagesAdapter(List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }


    @NonNull
    @Override
    public MessagesHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);
        MessagesHolder holder = new MessagesHolder(view);

        mAuth = FirebaseAuth.getInstance();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesHolder messagesHolder, final int position) {
        final String messageSenderId = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("User").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messagesHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        messagesHolder.tvReceiverMessage.setVisibility(View.GONE);
        messagesHolder.receiverProfileImage.setVisibility(View.GONE);
        messagesHolder.tvSenderMessage.setVisibility(View.GONE);
        messagesHolder.messageSenderPicture.setVisibility(View.GONE);
        messagesHolder.messageReceiverPicture.setVisibility(View.GONE);



        if (fromMessageType.equals("text")){

            if (fromUserID.equals(messageSenderId)){

                messagesHolder.tvSenderMessage.setVisibility(View.VISIBLE);

                messagesHolder.tvSenderMessage.setBackgroundResource(R.drawable.sender_messages_layout);
                messagesHolder.tvSenderMessage.setTextColor(Color.WHITE);
                messagesHolder.tvSenderMessage.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

            }else {
                messagesHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messagesHolder.tvReceiverMessage.setVisibility(View.VISIBLE);

                messagesHolder.tvReceiverMessage.setBackgroundResource(R.drawable.receiver_messages_layout);
                messagesHolder.tvReceiverMessage.setTextColor(Color.BLACK);
                messagesHolder.tvReceiverMessage.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderId)){

                messagesHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messagesHolder.messageSenderPicture);
            }
            else {

                messagesHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messagesHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messagesHolder.messageReceiverPicture);

            }
        }
        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx"))
        {
            if (fromUserID.equals(messageSenderId)){
                messagesHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                messagesHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/watched-afc9f.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=8d85b575-dbb0-4e2f-9e56-9c7b29a901b6")
                        .into(messagesHolder.messageSenderPicture);
            }
            else {
                messagesHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messagesHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                messagesHolder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/watched-afc9f.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=8d85b575-dbb0-4e2f-9e56-9c7b29a901b6")
                        .into(messagesHolder.messageReceiverPicture);

            }
        }
        if (fromUserID.equals(messageSenderId)){
            messagesHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMessage(position, messagesHolder);
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), MainActivity.class);
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMessage(position, messagesHolder);
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), MainActivity.class);
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 1){
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteSentMessage(position, messagesHolder);
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), MainActivity.class);
                                    messagesHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else{
            messagesHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")){
                        final CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiverMessage(position, messagesHolder);
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), MainActivity.class);
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                                if (i == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("image")){
                        final CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiverMessage(position, messagesHolder);
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), MainActivity.class);
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                                if (i == 1){
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessageList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteReceiverMessage(position, messagesHolder);
                                    Intent intent = new Intent(messagesHolder.itemView.getContext(), MainActivity.class);
                                    messagesHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList == null ? 0 : userMessageList.size();
    }

    public class MessagesHolder extends RecyclerView.ViewHolder {
        private TextView tvSenderMessage;
        private TextView tvReceiverMessage;
        private CircleImageView receiverProfileImage;

        private ImageView messageSenderPicture, messageReceiverPicture;

        public MessagesHolder(@NonNull View itemView) {
            super(itemView);

            tvSenderMessage = itemView.findViewById(R.id.sender_message_text);
            tvReceiverMessage = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);

            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
        }
    }

    private void deleteSentMessage(final int position, final MessagesHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successufully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiverMessage(final int position, final MessagesHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
