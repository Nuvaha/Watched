package com.example.watched;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView myContactList;

    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactList = (RecyclerView) contactsView.findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, contactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, contactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final contactsViewHolder holder, int position, @NonNull Contacts model) {
                        String userId = getRef(position).getKey();
                        userRef.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("image")){
                                    String userName = dataSnapshot.child("name").getValue().toString();
                                    String userStatus = dataSnapshot.child("status").getValue().toString();
                                    String userImage = dataSnapshot.child("image").getValue().toString();

                                    holder.userName.setText(userName);
                                    holder.userStatus.setText(userStatus);
                                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                }
                                else {
                                    String userName = dataSnapshot.child("name").getValue().toString();
                                    String userStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(userName);
                                    holder.userStatus.setText(userStatus);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup , false);
                        contactsViewHolder holder = new contactsViewHolder(view);
                        return holder;
                    }
                };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class contactsViewHolder extends RecyclerView.ViewHolder{

        private TextView userName;
        private TextView userStatus;
        private CircleImageView userImage;

        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_user_name);
            userStatus = itemView.findViewById(R.id.tv_user_status);
            userImage = itemView.findViewById(R.id.user_profile_name);
        }
    }
}
