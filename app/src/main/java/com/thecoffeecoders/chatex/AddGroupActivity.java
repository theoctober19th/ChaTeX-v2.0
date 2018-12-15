package com.thecoffeecoders.chatex;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thecoffeecoders.chatex.adapters.AddGroupFriendAdapter;
import com.thecoffeecoders.chatex.adapters.FriendRecyclerAdapter;
import com.thecoffeecoders.chatex.chat.GroupChatActivity;
import com.thecoffeecoders.chatex.interfaces.OnAdapterItemClicked;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.users.EditProfileActivity;
import com.thecoffeecoders.chatex.views.RecyclerViewWithEmptyView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddGroupActivity extends AppCompatActivity implements OnAdapterItemClicked {

    //Layout fields
    CircularImageView mGroupPhotoImgView;
    EditText mGroupNameEditText;
    RecyclerViewWithEmptyView mFriendListRecyclerView;
    FloatingActionButton mAddGroupBtn;
    ProgressBar mAddGroupPictureProgressBar;

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

    private String groupPicURI = null;
    private final int RC_IMAGE_PICKER_GROUP_PHOTO = 001;

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

        mGroupPhotoImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(300, 300)
                        .setFixAspectRatio(true)
                        .getIntent(AddGroupActivity.this);
                startActivityForResult(intent, RC_IMAGE_PICKER_GROUP_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            switch (requestCode){
                case RC_IMAGE_PICKER_GROUP_PHOTO :
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    mAddGroupPictureProgressBar.setVisibility(View.VISIBLE);
                    mAddGroupBtn.hide();

                    Uri groupPictureURI = result.getUri();
                    File originalPictureFile = new File(groupPictureURI.getPath());
                    File compressedPictureFile = originalPictureFile;
                    try {
                        compressedPictureFile = new Compressor(this)
                                .setMaxWidth(300)
                                .setMaxHeight(300)
                                .setQuality(75)
                                .compressToFile(originalPictureFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    RequestOptions requestOptions = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.img_profile_picture_placeholder_female);
                    Glide.with(AddGroupActivity.this)
                            .applyDefaultRequestOptions(requestOptions)
                            .load(groupPictureURI)
                            .into(mGroupPhotoImgView);

                    FirebaseStorage mStorage = FirebaseStorage.getInstance();
                    final StorageReference groupPictureRef =
                            mStorage.getReference()
                                    .child("group_images")
                                    .child(FirebaseDatabase
                                            .getInstance()
                                            .getReference()
                                            .child("keys")
                                            .push()
                                            .getKey()
                                    );
                    UploadTask uploadTask = groupPictureRef.putFile(Uri.fromFile(compressedPictureFile));
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return groupPictureRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                groupPicURI = downloadUri.toString();
                                mAddGroupPictureProgressBar.setVisibility(View.GONE);
                                mAddGroupBtn.show();
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                    break;
            }
        }
    }

    private void addNewGroupToFirebase() {
        String groupName = "";
        if(TextUtils.isEmpty(mGroupNameEditText.getText())){
            Toast.makeText(this, "Please enter the group name.", Toast.LENGTH_SHORT).show();
            return;
        }else if(membersMap.size() < 1 ){
            Toast.makeText(this, "Add at least one other person", Toast.LENGTH_SHORT).show();
            return;
        } else{
            groupName = mGroupNameEditText.getText().toString();
        }
        mGroup.setName(groupName);
        mGroup.setMemberCount(userCount);
        mGroup.setCreator(mAuth.getUid());
        mGroup.setGroupPicURI(groupPicURI);
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
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(mFriendsRef, Friend.class)
                .build();

        mFirebaseRecyclerAdapter = new AddGroupFriendAdapter(options, this);
        mFriendListRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
    }

    private void initializeLayoutFieldsAndFirebaseObjects(){
        mGroupPhotoImgView = findViewById(R.id.add_group_group_picture);
        mGroupNameEditText = findViewById(R.id.add_group_group_name_et);
        mAddGroupBtn = findViewById(R.id.add_group_fab);
        mFriendListRecyclerView = (RecyclerViewWithEmptyView) findViewById(R.id.add_group_friend_list_recyclerview);
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        View emptyView = findViewById(R.id.activity_add_group_empty_view);
        mFriendListRecyclerView.setEmptyView(emptyView);
        mAddGroupPictureProgressBar = findViewById(R.id.add_group_picture_progress_bar);

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

    @Override
    public void onAdapterItemClicked(String value) {
        Log.d("uid_clicked", value);
        if(membersMap.containsKey(value)){
            membersMap.remove(value);
            userCount --;
        }else{
            userCount ++;
            membersMap.put(value, memberInfoMap);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseRecyclerAdapter.stopListening();
    }
}
