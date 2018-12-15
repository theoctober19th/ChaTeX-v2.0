package com.thecoffeecoders.chatex.chat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.MessageRecyclerAdapter;
import com.thecoffeecoders.chatex.math.WriteEquationActivity;
import com.thecoffeecoders.chatex.misc.SendLocation;
import com.thecoffeecoders.chatex.models.Chat;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.models.Request;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.users.UserProfileActivity;
import com.thecoffeecoders.chatex.views.RecyclerViewWithEmptyView;
//import com.thecoffeecoders.chatex.utils.Utils;
//import com.zhihu.matisse.Matisse;
//import com.zhihu.matisse.MimeType;
//import com.zhihu.matisse.engine.impl.GlideEngine;
//import com.zhihu.matisse.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    //Views
    private EditText mTypeMessageEditText;
    private FloatingActionButton mSendImageButton;
    private RecyclerViewWithEmptyView mChatRecyclerView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mChatExtensionBtn;
    private LinearLayout mChatInputLayout;
    private LinearLayout mRequestOptionsLayout;
    android.support.v7.widget.Toolbar mToolbar;

    private MessageRecyclerAdapter mMessageAdapter;

    private boolean isFriend = true;

    //Firebase Objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String currentUserID;
    String otherUserProfilePicURI;
    long otherUserLastOnline;
    String otherUserDisplayName;
    String otherUserID;

    private static String POPUP_CONSTANT = "mPopup";
    private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
    private final int RC_LATEX_EQUATION = 001;
    private final int RC_LOCATION_MSG = 002;
    private final int RC_PICK_IMAGE = 003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        initializeFirebaseObjects();

        checkIfFriends();
        FirebaseDatabase.getInstance()
                .getReference()
                .child("requests")
                .child(currentUserID)
                .child(otherUserID)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists() && dataSnapshot.getValue(Request.class).getType().equals("received")){
                                    mChatInputLayout.setVisibility(View.GONE);
                                    mRequestOptionsLayout.setVisibility(View.VISIBLE);
                                    addRequestButtonListeners();
                                }else{
                                    mChatInputLayout.setVisibility(View.VISIBLE);
                                    mRequestOptionsLayout.setVisibility(View.GONE);
                                }
                                mSendImageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String messageContent = mTypeMessageEditText.getText().toString();
                                        if(!TextUtils.isEmpty(messageContent)){
                                            sendMessage(messageContent, "text");
                                        }
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }
                );

        setToolbar();
    }

    private void addRequestButtonListeners() {
        Button acceptButton = findViewById(R.id.activity_chat_accept_request_btn);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("friendlist/" + currentUserID + "/" + otherUserID, new Friend("1"));
                childUpdates.put("friendlist/" + otherUserID + "/" + currentUserID, new Friend("1"));
                childUpdates.put("requests/" + currentUserID + "/" + otherUserID, null);
                childUpdates.put("requests/" + otherUserID + "/" + currentUserID, null);
                dbRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mRequestOptionsLayout.setVisibility(View.GONE);
                            mChatInputLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        Button rejectButton = findViewById(R.id.activity_chat_reject_request_btn);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("friendlist/" + currentUserID + "/" + otherUserID, null);
                childUpdates.put("friendlist/" + otherUserID + "/" + currentUserID, null);
                childUpdates.put("requests/" + currentUserID + "/" + otherUserID, null);
                childUpdates.put("requests/" + otherUserID + "/" + currentUserID, null);
                childUpdates.put("messages/" + currentUserID + "/" + otherUserID, null);
                dbRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mRequestOptionsLayout.setVisibility(View.GONE);
                            mChatInputLayout.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void setToolbar() {
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("HEHE");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseReference otherUserRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(otherUserID);
        otherUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User otherUser = dataSnapshot.getValue(User.class);
                    otherUserProfilePicURI = otherUser.getProfilePicURI();
                    otherUserDisplayName = otherUser.getDisplayName();
                    otherUserLastOnline = otherUser.getLastOnline();
                    //otherUserProfilePicURI = dataSnapshot.getValue().toString();

                    CircularImageView appbarImage = findViewById(R.id.chat_appbar_image);
                    Glide.with(ChatActivity.this)
                            .applyDefaultRequestOptions(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .load(otherUserProfilePicURI)
                            .into(appbarImage);
                    TextView appBarDisplayName = findViewById(R.id.chat_appbar_name);
                    appBarDisplayName.setText(otherUserDisplayName);
                    TextView appBarLastOnline = findViewById(R.id.chat_appbar_lastOnline);
                    //appBarLastOnline.setText("TODO : Last online");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getOtherUserData() {
    }

    public void checkIfFriends(){
        DatabaseReference friendRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("friendlist")
                .child(currentUserID)
                .child(otherUserID);
        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    isFriend = false;
                    //mChatInputLayout.setVisibility(View.GONE);
                    //mRequestOptionsLayout.setVisibility(View.VISIBLE);
                }else{
                    isFriend = true;
                    //mRequestOptionsLayout.setVisibility(View.GONE);
                    //mChatInputLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addAdapter(){
        Query msgquery = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("messages").child(currentUserID).child(otherUserID)
                .orderByChild("timestamp");
        DatabaseReference msgRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("messages").child(currentUserID).child(otherUserID);
        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(msgRef, Message.class)
                .build();
        mMessageAdapter = new MessageRecyclerAdapter(options, currentUserID, otherUserID/*, otherUserProfilePicURI*/);
        mChatRecyclerView.setAdapter(mMessageAdapter);
        mChatRecyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        addAdapter();
        mMessageAdapter.startListening();
        mProgressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessageAdapter.stopListening();
    }

    private void initializeViews(){
        mTypeMessageEditText = findViewById(R.id.et_chatmessage);
        mSendImageButton = findViewById(R.id.fabSendMessage);
        mChatRecyclerView = (RecyclerViewWithEmptyView) findViewById(R.id.chatMessageRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(layoutManager);
        View emptyView = findViewById(R.id.activity_chat_empty_view);
        mChatRecyclerView.setEmptyView(emptyView);

        mProgressBar = findViewById(R.id.chat_activity_progress_bar);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mChatInputLayout = findViewById(R.id.chat_input_layout);
        mToolbar = findViewById(R.id.chat_appbar);
        mRequestOptionsLayout = findViewById(R.id.chat_request_layout);

        //mChatInputLayout.setVisibility(View.VISIBLE);
        //mRequestOptionsLayout.setVisibility(View.VISIBLE);
    }

    private void initializeFirebaseObjects(){
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(getIntent().getStringExtra("uid")!=null){
            otherUserID = getIntent().getStringExtra("uid");
        }
        currentUserID = mUser.getUid();
    }

    private void sendMessage(String messageContent, String type) {
        if(!TextUtils.isEmpty(messageContent)){
            final Message message = new Message();
            message.setContent(messageContent);
            message.setType(type);
            message.setSender(currentUserID);
            message.setTimestamp(System.currentTimeMillis());

            DatabaseReference msgRef1 = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("messages").child(currentUserID).child(otherUserID);
            final DatabaseReference msgRef2 = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("messages").child(otherUserID).child(currentUserID);
            final DatabaseReference chatRef1 = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("chat").child(currentUserID).child(otherUserID);
            final DatabaseReference chatRef2 = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("chat").child(otherUserID).child(currentUserID);
            final DatabaseReference requestRef1 = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("requests").child(otherUserID).child(currentUserID);
            final DatabaseReference requestRef2 = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("requests").child(currentUserID).child(otherUserID);

            final String key = msgRef1.push().getKey();

            msgRef1.child(key).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        msgRef2.child(key).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    if(isFriend){
                                        final Chat chatReciever = new Chat();
                                        chatReciever.setSeen(false);
                                        chatReciever.setSeenTimestamp(-1*System.currentTimeMillis());
                                        chatReciever.setLastMessage(key);

                                        final Chat chatSender = new Chat();
                                        chatSender.setSeen(true);
                                        chatSender.setSeenTimestamp(-1*System.currentTimeMillis());
                                        chatSender.setLastMessage(key);

                                        chatRef1.setValue(chatSender).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    chatRef2.setValue(chatReciever).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }else{
                                        final Request sentRequest = new Request();
                                        sentRequest.setType("sent");
                                        sentRequest.setTimestamp(System.currentTimeMillis());
                                        requestRef2.setValue(sentRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                final Request receivedRequest = new Request();
                                                receivedRequest.setType("received");
                                                receivedRequest.setTimestamp(System.currentTimeMillis());
                                                requestRef1.setValue(receivedRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                                //mMessageAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }
        clearInput();
    }

    public void openProfile(View view){
        Intent profileIntent = new Intent(this, UserProfileActivity.class);
        profileIntent.putExtra("uid", otherUserID);
        startActivity(profileIntent);
    }

    public void clearInput(){
        mTypeMessageEditText.setText("");
    }

    public void showPopup(View view) {
//        PopupMenu popup = new PopupMenu(this, v);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.chat_extensions_menu, popup.getMenu());

        PopupMenu popup = new PopupMenu(ChatActivity.this, view);
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
                        Intent sendLocationIntent = new Intent(ChatActivity.this, SendLocation.class);
                        startActivityForResult(sendLocationIntent, RC_LOCATION_MSG);
                        break;
                    case R.id.ext_menu_math:
                        Intent writeEquationIntent = new Intent(ChatActivity.this, WriteEquationActivity.class);
                        startActivityForResult(writeEquationIntent, RC_LATEX_EQUATION);
                        break;
                    case R.id.ext_menu_photo:
//                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                        photoPickerIntent.setType("image/*");
//                        startActivityForResult(photoPickerIntent, RC_PICK_IMAGE);

                        Intent intent = CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .getIntent(ChatActivity.this);
                        startActivityForResult(intent, RC_PICK_IMAGE);

                        //Toast.makeText(ChatActivity.this, "TODO: Fetch Image From Gallery", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
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
                    Toast.makeText(ChatActivity.this, "MESSAGE SENT", Toast.LENGTH_SHORT).show();
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    public void fetchImageFromDevice(){
//        Matisse.from(ChatActivity.this)
//                .choose(MimeType.allOf())
//                .countable(true)
//                .maxSelectable(9)
//                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                .thumbnailScale(0.85f)
//                .imageEngine(new GlideEngine())
//                .forResult(REQUEST_CODE_CHOOSE);
    }
}
