package com.example.watched.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.watched.R;
import com.example.watched.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar findFriendsToolbar;
    private RecyclerView findFriendsRecyclerList;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        findFriendsToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(findFriendsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        userRef = FirebaseDatabase.getInstance().getReference().child("User");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, findFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, findFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.tvUserName.setText(model.getName());
                holder.tvUserStatus.setText(model.getStatus());

                Picasso.get().load(model.getImage()).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileActivity = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileActivity.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileActivity);
                    }
                });

            }

            @NonNull
            @Override
            public findFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                findFriendViewHolder holder = new findFriendViewHolder(view);
                return holder;
            }
        };
        findFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class findFriendViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUserName;
        private TextView tvUserStatus;
        private CircleImageView profileImage;

        public findFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserStatus = itemView.findViewById(R.id.tv_user_status);
            profileImage = itemView.findViewById(R.id.user_profile_name);
        }
    }
}
