package com.thecoffeecoders.chatex.chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.GroupMessageRecyclerAdapter;
import com.thecoffeecoders.chatex.adapters.MessageRecyclerAdapter;
import com.thecoffeecoders.chatex.math.WriteEquationActivity;
import com.thecoffeecoders.chatex.misc.SendLocation;
import com.thecoffeecoders.chatex.models.Chat;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.models.Request;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.users.UserProfileActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GroupChatActivity extends AppCompatActivity {

    //Views
    private EditText mTypeMessageEditText;
    private FloatingActionButton mSendImageButton;
    private RecyclerView mChatRecyclerView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mChatExtensionBtn;
    private LinearLayout mChatInputLayout;
    private LinearLayout mRequestOptionsLayout;
    android.support.v7.widget.Toolbar mToolbar;

    private GroupMessageRecyclerAdapter mMessageAdapter;

    private boolean isFriend = true;

    //Firebase Objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String currentUserID;
    String groupID;

    private static String POPUP_CONSTANT = "mPopup";
    private static String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
    private int RC_LATEX_EQUATION = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initializeViews();
        initializeFirebaseObjects();

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

        //checkIfFriends();
        setToolbar();
        addAdapter();
    }

//    public void checkIfFriends(){
//        DatabaseReference friendRef = FirebaseDatabase
//                .getInstance()
//                .getReference()
//                .child("friendlist")
//                .child(currentUserID)
//                .child(otherUserID);
//        friendRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(!dataSnapshot.exists()){
//                    isFriend = false;
//                    //mChatInputLayout.setVisibility(View.GONE);
//                    //mRequestOptionsLayout.setVisibility(View.VISIBLE);
//                }else{
//                    //isFriend = true;
//                    //mRequestOptionsLayout.setVisibility(View.GONE);
//                    //mChatInputLayout.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    public void addAdapter(){
//        Query msgquery = FirebaseDatabase
//                .getInstance()
//                .getReference()
//                .child("messages").child(currentUserID).child(groupID)
//                .orderByChild("timestamp");
        DatabaseReference msgRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groupmessages").child(groupID);
        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(msgRef, Message.class)
                .build();
        mMessageAdapter = new GroupMessageRecyclerAdapter(options);
        mChatRecyclerView.setAdapter(mMessageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMessageAdapter.startListening();
        mProgressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessageAdapter.stopListening();
    }

    private void initializeViews(){
        mTypeMessageEditText = findViewById(R.id.et_chatmessage_groupchat);
        mSendImageButton = findViewById(R.id.fabSendMessage_groupchat);
        mChatRecyclerView = findViewById(R.id.groupChatMessageRecyclerView);
        mProgressBar = findViewById(R.id.groupchat_activity_progress_bar);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mChatInputLayout = findViewById(R.id.groupchat_input_layout);
        mToolbar = findViewById(R.id.groupchat_appbar);
        //mRequestOptionsLayout = findViewById(R.id.request_options_layout);

        //mChatInputLayout.setVisibility(View.VISIBLE);
        //mRequestOptionsLayout.setVisibility(View.VISIBLE);
    }

    private void initializeFirebaseObjects(){
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(getIntent().getStringExtra("groupid")!=null){
            groupID = getIntent().getStringExtra("groupid");
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

    public void openProfile(View view){
//        Intent profileIntent = new Intent(this, UserProfileActivity.class);
//        profileIntent.putExtra("uid", otherUserID);
//        startActivity(profileIntent);
    }

    public void clearInput(){
        mTypeMessageEditText.setText("");
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
                        startActivity(sendLocationIntent);
                        break;
                    case R.id.ext_menu_math:
                        Intent writeEquationIntent = new Intent(GroupChatActivity.this, WriteEquationActivity.class);
                        startActivityForResult(writeEquationIntent, RC_LATEX_EQUATION);
                        break;
                    case R.id.ext_menu_photo:
                        Toast.makeText(GroupChatActivity.this, "TODO: Fetch Image From Gallery", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_LATEX_EQUATION){
            if(resultCode == RESULT_OK){
                String latexCode = data.getStringExtra("equation");
                sendMessage(latexCode, "math");
            }
        }
    }

    private void setToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    String groupName = group.getName();
                    int memberCount = group.getMemberCount();

                    CircularImageView appbarImage = findViewById(R.id.chat_appbar_image);
                    Glide.with(GroupChatActivity.this)
                            .applyDefaultRequestOptions(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .load(groupPicURI)
                            .into(appbarImage);
                    TextView appBarDisplayName = findViewById(R.id.chat_appbar_name);
                    appBarDisplayName.setText(groupName);
                    TextView appBarLastOnline = findViewById(R.id.chat_appbar_lastOnline);
                    //appBarLastOnline.setText("TODO : Last online");
                    appBarLastOnline.setText(String.valueOf(memberCount) + " members");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

