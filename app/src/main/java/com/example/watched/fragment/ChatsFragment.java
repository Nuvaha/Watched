package com.example.watched.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.watched.model.Contacts;
import com.example.watched.R;
import com.example.watched.main.ChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference chatsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUaerId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = privateChatsView.findViewById(R.id.chat_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUaerId = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUaerId);
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String userId = getRef(position).getKey();
                        final String[] imProfile = {"default_image"};

                        userRef.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){
                                        imProfile[0] = dataSnapshot.child("image").getValue().toString();

                                        Picasso.get().load(imProfile[0]).placeholder(R.drawable.profile_image).into(holder.imProfile);
                                    }

                                    final String tvUserName = dataSnapshot.child("name").getValue().toString();

                                    holder.tvUserName.setText(tvUserName);


                                    if (dataSnapshot.child("userState").hasChild("state")){
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();

                                        holder.tvUserStatus.setText(state);

                                        if (state.equals("online")){
                                            String tvUserStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.tvUserStatus.setText(tvUserStatus);
                                            holder.onlineIocon.setVisibility(View.VISIBLE);

                                        }
                                        else if (state.equals("offline")){
                                           // holder.tvUserStatus.setText("Last seen: " + time + " " + date);
                                            String tvUserStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.tvUserStatus.setText(tvUserStatus);
                                            holder.onlineIocon.setVisibility(View.INVISIBLE);
                                        }
                                    }else {
                                        String tvUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.tvUserStatus.setText(tvUserStatus);
                                        holder.tvUserStatus.setText("Offline");
                                        holder.onlineIocon.setVisibility(View.INVISIBLE);
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userId);
                                            chatIntent.putExtra("visit_user_name", tvUserName);
                                            chatIntent.putExtra("visit_image", imProfile[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        ChatsViewHolder holder = new ChatsViewHolder(view);
                        return holder;
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUserName;
        private TextView tvUserStatus;
        private CircleImageView imProfile;
        private ImageView onlineIocon;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserStatus = itemView.findViewById(R.id.tv_user_status);
            imProfile = itemView.findViewById(R.id.user_profile_name);
            onlineIocon = itemView.findViewById(R.id.image_user_online_status);
        }
    }


}
