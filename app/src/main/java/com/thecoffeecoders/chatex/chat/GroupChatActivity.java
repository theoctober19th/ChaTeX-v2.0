package com.thecoffeecoders.chatex.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.GroupMemberAdapter;
import com.thecoffeecoders.chatex.adapters.GroupMessageRecyclerAdapter;
import com.thecoffeecoders.chatex.interfaces.OnAdapterItemClicked;
import com.thecoffeecoders.chatex.math.WriteEquationActivity;
import com.thecoffeecoders.chatex.misc.SendLocation;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.views.EndDrawerToggle;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import id.zelory.compressor.Compressor;

public class GroupChatActivity extends AppCompatActivity {

    DrawerLayout mDrawerLayout;
    //Views
    private EditText mTypeMessageEditText;
    private FloatingActionButton mSendImageButton;
    private RecyclerView mChatRecyclerView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mChatExtensionBtn;
    private RecyclerView mGroupMemberRecyclerView;

    private GroupMemberAdapter mGropuMemberAdapter;
    private GroupMessageRecyclerAdapter mMessageAdapter;

    //Firebase Objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String currentUserID;
    String groupID;

    private static String POPUP_CONSTANT = "mPopup";
    private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
    private final int RC_LATEX_EQUATION = 001;
    public static final int RC_LOCATION_MSG = 002;
    public static final int RC_PICK_IMAGE = 003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.groupchatappbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeViews();
        initializeFirebaseObjects();
        setToolbar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.groupchat_drawer_layout);
        EndDrawerToggle toggle = new EndDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.groupchat_nav_view);
        mGroupMemberRecyclerView = navigationView.getHeaderView(0).findViewById(R.id.group_member_recyclerview);

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = mTypeMessageEditText.getText().toString();
                if(!TextUtils.isEmpty(messageContent)){
                    sendMessage(messageContent, "text");
                }
            }
        });
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mGroupMemberRecyclerView.setLayoutManager(linearLayoutManager);
        addAdapter();

    }

    private void initializeViews(){
        mTypeMessageEditText = findViewById(R.id.et_chatmessage_groupchat);
        mSendImageButton = findViewById(R.id.fabSendMessage_groupchat);
        mChatRecyclerView = findViewById(R.id.groupChatMessageRecyclerView);
        mProgressBar = findViewById(R.id.groupchat_activity_progress_bar);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void initializeFirebaseObjects(){
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(getIntent().getStringExtra("groupid")!=null){
            groupID = getIntent().getStringExtra("groupid");
        }
        currentUserID = mUser.getUid();
    }

    public void setToolbar(){


        DatabaseReference otherUserRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groups")
                .child(groupID);
        otherUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Group group = dataSnapshot.getValue(Group.class);
                    String groupPicURI = group.getGroupPicURI();
                    String groupname = group.getName();
                    int membercount = group.getMemberCount();
                    //otherUserProfilePicURI = dataSnapshot.getValue().toString();

                    CircularImageView appbarImage = findViewById(R.id.groupchat_appbar_image);
                    Glide.with(GroupChatActivity.this)
                            .applyDefaultRequestOptions(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .load(groupPicURI)
                            .into(appbarImage);
                    TextView appBarDisplayName = findViewById(R.id.groupchat_appbar_name);
                    appBarDisplayName.setText(groupname);
                    TextView appBarLastOnline = findViewById(R.id.groupchat_appbar_membercount);
//                    appBarLastOnline.setText(String.valueOf(membercount) + " members");
                    appBarLastOnline.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addAdapter(){
        DatabaseReference msgRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groupmessages").child(groupID);
        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(msgRef, Message.class)
                .build();
        mMessageAdapter = new GroupMessageRecyclerAdapter(options);


        DatabaseReference groupMemberRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groupmembers")
                .child(groupID);
        FirebaseRecyclerOptions<Boolean> options1 = new FirebaseRecyclerOptions.Builder<Boolean>()
                .setQuery(groupMemberRef, Boolean.class)
                .build();
        mGropuMemberAdapter = new GroupMemberAdapter(options1, groupID);
    }


        @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGroupMemberRecyclerView.setAdapter(mGropuMemberAdapter);
        mGropuMemberAdapter.startListening();
        mChatRecyclerView.setAdapter(mMessageAdapter);
        mMessageAdapter.startListening();
        mProgressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessageAdapter.stopListening();
    }

    private void sendMessage(String messageContent, String type) {
        if(!TextUtils.isEmpty(messageContent)){
            final Message message = new Message();
            message.setContent(messageContent);
            message.setType(type);
            message.setSender(currentUserID);
            message.setTimestamp(System.currentTimeMillis());

            DatabaseReference groupMessageRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("groupmessages")
                    .child(groupID);

            final String key = groupMessageRef.push().getKey();

            groupMessageRef.child(key).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Written Successfully
                        mMessageAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        clearInput();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch(requestCode){
                case RC_LATEX_EQUATION:
                    String latexCode = data.getStringExtra("equation");
                    sendMessage(latexCode, "math");
                    break;
                case RC_LOCATION_MSG:
                    String locationData = data.getStringExtra("location");
                    sendMessage(locationData, "location");
                    break;
                case RC_PICK_IMAGE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri pickedImageUri = result.getUri();
                    /*String imageMsg = */uploadImage(pickedImageUri);
//                    sendMessage(imageMsg, "image");
                    break;
            }
        }
    }

    private void uploadImage(Uri pickedImageUri) {
        File originalPictureFile = new File(pickedImageUri.getPath());
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

        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        String key = FirebaseDatabase.getInstance().getReference().child("keys").push().getKey();
        final StorageReference imageStorageRef =
                mStorage.getReference()
                        .child("user_message_images")
                        .child(mUser.getUid())
                        .child(key);
        UploadTask uploadTask = imageStorageRef.putFile(Uri.fromFile(compressedPictureFile));

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageStorageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    sendMessage(downloadUri.toString(), "image");
                    Toast.makeText(GroupChatActivity.this, "MESSAGE SENT", Toast.LENGTH_SHORT).show();
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    public void showPopup(View view) {
//        PopupMenu popup = new PopupMenu(this, v);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.chat_extensions_menu, popup.getMenu());

        PopupMenu popup = new PopupMenu(GroupChatActivity.this, view);
        try {
            // Reflection apis to enforce show icon
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(POPUP_CONSTANT)) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.chat_extensions_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.ext_menu_location:
                        Intent sendLocationIntent = new Intent(GroupChatActivity.this, SendLocation.class);
                        startActivityForResult(sendLocationIntent, RC_LOCATION_MSG);
                        break;
                    case R.id.ext_menu_math:
                        Intent writeEquationIntent = new Intent(GroupChatActivity.this, WriteEquationActivity.class);
                        startActivityForResult(writeEquationIntent, RC_LATEX_EQUATION);
                        break;
                    case R.id.ext_menu_photo:
                        Intent intent = CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .getIntent(GroupChatActivity.this);
                        startActivityForResult(intent, RC_PICK_IMAGE);
                }
                return false;
            }
        });
        popup.show();
    }

    public void clearInput(){
        mTypeMessageEditText.setText("");
    }




    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        //getMenuInflater().inflate(R.menu.group_chat, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    protected void toggleDrawer(View view){
        mDrawerLayout.openDrawer(GravityCompat.END);
    }
}
