package com.thecoffeecoders.chatex;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.chat.GroupChatActivity;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.models.Group;

import java.util.HashMap;
import java.util.Map;

public class AddGroupActivity extends AppCompatActivity {

    //Layout fields
    CircularImageView mGroupPhotoImgView;
    EditText mGroupNameEditText;
    RecyclerView mFriendListRecyclerView;
    FloatingActionButton mAddGroupBtn;

    //Firebase Objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mFriendsRef;
    DatabaseReference mGroupRef;
    DatabaseReference mUserRef;
    DatabaseReference mGroupUserRef;
    DatabaseReference mUserGroupRef;
    FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;

    Map<String, Object> membersMap = new HashMap<>();
    Map<String, Object> memberInfoMap = new HashMap<>();

    private int userCount = 1;
    private Group mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        initializeLayoutFieldsAndFirebaseObjects();
        mGroup = new Group();
        mAddGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGroupToFirebase();
            }
        });
    }

    private void addNewGroupToFirebase() {
        String groupName = "";
        if(TextUtils.isEmpty(mGroupNameEditText.getText())){
            Toast.makeText(this, "Please enter the group name.", Toast.LENGTH_SHORT).show();
            return;
        }else{
            groupName = mGroupNameEditText.getText().toString();
        }
        mGroup.setName(groupName);
        mGroup.setMemberCount(userCount);
        mGroup.setGroupPicURI("https://us.123rf.com/450wm/yurich84/yurich841805/yurich84180500120/102048648-html-header-markup-extreme-close-up-coding-and-programming-concept.jpg?ver=6");
        mGroup.setCreated(System.currentTimeMillis());
        membersMap.put(mUser.getUid(), memberInfoMap);
        final String groupKey = mGroupRef.push().getKey();
        mGroup.setId(groupKey);
        mGroupRef.child(groupKey).setValue(mGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mGroupUserRef.child(groupKey).updateChildren(membersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Map<String, Object> thisGroupTrueMap = new HashMap<>();
                                thisGroupTrueMap.put(groupKey, true);
                                Map<String, Object> usersGroupMap = new HashMap<>();
                                for (Map.Entry<String, Object> entry: membersMap.entrySet()){
                                    mUserGroupRef.child(entry.getKey()).updateChildren(thisGroupTrueMap);
                                }
//                                mUserGroupRef.updateChildren(usersGroupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if(task.isSuccessful()){
//
//                                        }else{
//                                            Toast.makeText(AddGroupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
                                takeUserToGroupChatActivity(groupKey);
                            }else{
                                Toast.makeText(AddGroupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(AddGroupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void takeUserToGroupChatActivity(String groupKey) {
        Intent groupChatIntent = new Intent(this, GroupChatActivity.class);
        groupChatIntent.putExtra("groupid", groupKey);
        groupChatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(groupChatIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        addFirebaseAdapterForFriendList();
        mFirebaseRecyclerAdapter.startListening();
    }

    private void addFirebaseAdapterForFriendList(){
        //FirebaseRecyclerAdapter
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(mFriendsRef, Friend.class)
                .build();

        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friend model) {
                holder.setFriendsSince(model.getSince());

                final String list_user_id = getRef(position).getKey();
                Log.d("bikalpa", list_user_id);
                mUserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        String profilePicURI = dataSnapshot.child("profilePicURI").getValue().toString();
                        String online = dataSnapshot.child("onlineStatus").getValue().toString();
                        String displayName = dataSnapshot.child("displayName").getValue().toString();
                        holder.setDisplayName(displayName);
                        holder.setUserName(username);
                        holder.setProfilePicture(profilePicURI);
                        holder.setOnline(online);
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.changeSelected(list_user_id);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }


            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.user_single_layout, viewGroup, false);

                return new FriendsViewHolder(view);
            }
        };
        mFriendListRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
    }

    private void initializeLayoutFieldsAndFirebaseObjects(){
        mGroupPhotoImgView = findViewById(R.id.add_group_group_picture);
        mGroupNameEditText = findViewById(R.id.add_group_group_name_et);
        mAddGroupBtn = findViewById(R.id.add_group_fab);
        mFriendListRecyclerView = findViewById(R.id.add_group_friend_list_recyclerview);
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("friendlist").child(mUser.getUid());
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        mGroupUserRef = FirebaseDatabase.getInstance().getReference().child("groupmembers");
        mUserGroupRef = FirebaseDatabase.getInstance().getReference().child("usergroups");

        memberInfoMap.put("joined", System.currentTimeMillis());
        memberInfoMap.put("nickname", "");
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private boolean isSelected;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            isSelected = false;
        }

        public void setDisplayName(String displayName){
            TextView nameTextView = (TextView)mView.findViewById(R.id.user_single_name);
            nameTextView.setText(displayName);
        }

        public void setFriendsSince(String since){
            TextView dateTextView = (TextView)mView.findViewById(R.id.user_single_bio);
            dateTextView.setText(since);
        }

        public void setUserName(String userName){
            TextView usernameTextView = (TextView)mView.findViewById(R.id.user_single_username);
            usernameTextView.setText(userName);
        }

        public void setOnline(String online){
            CircularImageView onlineIcon = (CircularImageView)mView.findViewById(R.id.user_single_online_icon);
            if (online.equals("true")){
                onlineIcon.setVisibility(View.VISIBLE);
            }else{
                onlineIcon.setVisibility(View.INVISIBLE);
            }
        }

        public void setProfilePicture(String profilePicURI){
            CircularImageView profilePicture = mView.findViewById(R.id.user_single_image);

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_profile_picture_placeholder_female);
            Glide.with(AddGroupActivity.this)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(profilePicURI)
                    .into(profilePicture);
        }

        public void setSelected(){
            RelativeLayout singleUserLayout = mView.findViewById(R.id.user_single_layout);
            singleUserLayout.setBackgroundColor(Color.LTGRAY);
        }

        public void removeSelected(){
            RelativeLayout singleUserLayout = mView.findViewById(R.id.user_single_layout);
            singleUserLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        public void changeSelected(String userID){
            if(isSelected){
                removeSelected();
                isSelected = false;
                membersMap.remove(userID);
                userCount--;
            }else{
                setSelected();
                isSelected = true;
                userCount++;
                membersMap.put(userID, memberInfoMap);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseRecyclerAdapter.stopListening();
    }
}
