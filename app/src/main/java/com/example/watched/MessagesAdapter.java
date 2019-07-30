package com.example.watched;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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
    public void onBindViewHolder(@NonNull final MessagesHolder messagesHolder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(i);

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
        if (fromMessageType.equals("text")){
            messagesHolder.tvReceiverMessage.setVisibility(View.INVISIBLE);
            messagesHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

            messagesHolder.tvSenderMessage.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderId)){

                messagesHolder.tvSenderMessage.setVisibility(View.VISIBLE);

                messagesHolder.tvSenderMessage.setBackgroundResource(R.drawable.sender_messages_layout);
                messagesHolder.tvSenderMessage.setTextColor(Color.WHITE);
                messagesHolder.tvSenderMessage.setText(messages.getMessage());

            }else {
                messagesHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messagesHolder.tvReceiverMessage.setVisibility(View.VISIBLE);

                messagesHolder.tvReceiverMessage.setBackgroundResource(R.drawable.receiver_messages_layout);
                messagesHolder.tvReceiverMessage.setTextColor(Color.BLACK);
                messagesHolder.tvReceiverMessage.setText(messages.getMessage());
            }
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

        public MessagesHolder(@NonNull View itemView) {
            super(itemView);

            tvSenderMessage = itemView.findViewById(R.id.sender_message_text);
            tvReceiverMessage = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }
}
