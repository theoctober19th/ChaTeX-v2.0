package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.chat.GroupChatActivity;
import com.thecoffeecoders.chatex.misc.Constants;
import com.thecoffeecoders.chatex.models.Chat;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.users.UserProfileActivity;
import com.thecoffeecoders.chatex.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<Chat, RecyclerView.ViewHolder> {

    Context mContext;
    ProgressBar mProgressBar;

    public ChatRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Chat> options, Context context, ProgressBar progressBar) {
        super(options);
        mContext = context;
        mProgressBar = progressBar;
    }

    @Override
    protected void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position, @NonNull final Chat model) {
        final String uid = getRef(position).getKey();
        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final User user = dataSnapshot.getValue(User.class);
                    DatabaseReference lastMessageRef = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("messages").child(FirebaseAuth.getInstance().getUid())
                            .child(user.getId())
                            .child(model.getLastMessage());
                    lastMessageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Message message = dataSnapshot.getValue(Message.class);
                                final String content = message.getContent();
                                final String type = message.getType();
                                final long timestamp = message.getTimestamp();
                                ((ChatViewHolder)holder).bind(user, content, type, timestamp, model.isSeen());
                                ((ChatViewHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //take user to UserProfileActivity
                                        Intent chatIntent = new Intent(v.getContext(), ChatActivity.class);
                                        chatIntent.putExtra("uid", uid);
                                        mContext.startActivity(chatIntent);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }else{
                    final String groupid = uid;
                    DatabaseReference groupRef = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("groups").child(groupid);
                    groupRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                final Group group = dataSnapshot.getValue(Group.class);
                                DatabaseReference lastMessageRef = FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("messages").child(FirebaseAuth.getInstance().getUid())
                                        .child(group.getId())
                                        .child(model.getLastMessage());
                                lastMessageRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            Message message = dataSnapshot.getValue(Message.class);
                                            final String content = message.getContent();
                                            final String type = message.getType();
                                            final long timestamp = message.getTimestamp();
                                            ((ChatViewHolder)holder).bind(group, content, type, timestamp, model.isSeen());
                                            ((ChatViewHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent groupChatIntent = new Intent(v.getContext(), GroupChatActivity.class);
                                                    groupChatIntent.putExtra("groupid", groupid);
                                                    mContext.startActivity(groupChatIntent);
                                                    //take user to UserProfileActivity
//                                                    Intent chatIntent = new Intent(v.getContext(), ChatActivity.class);
//                                                    chatIntent.putExtra("uid", uid);
//                                                    mContext.startActivity(chatIntent);
                                                    Toast.makeText(mContext, "TODO: click here goes to grouppage", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }else{




                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_single_convo, viewGroup, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        if (mProgressBar != null) {
            if (mProgressBar.getVisibility() == ProgressBar.VISIBLE) {
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView;
        TextView lastMsgTextView;
        TextView usernameTextView;
        CircularImageView onlineIcon;
        CircularImageView profilePicture;
        TextView dateTextView;
        Context context;
        View view;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = (TextView)itemView.findViewById(R.id.friend_single_name);
            lastMsgTextView = (TextView)itemView.findViewById(R.id.convo_single_lastMessage);
            usernameTextView = (TextView)itemView.findViewById(R.id.convo_single_username);
            onlineIcon  = (CircularImageView)itemView.findViewById(R.id.convo_single_online_icon);
            profilePicture = itemView.findViewById(R.id.friend_single_image);
            dateTextView = itemView.findViewById(R.id.convo_single_date);
            context = itemView.getContext();
            view = itemView;
        }

        public void bind(final User user, String lastMessageContent, String type, long timestamp, boolean seen){
            nameTextView.setText(user.getDisplayName());
            usernameTextView.setText("@" + user.getUsername());
            String time = Utils.convertTimestampToDate(timestamp);
            dateTextView.setText(time);
            if(user.isOnlineStatus()){
                onlineIcon.setVisibility(View.VISIBLE);
            }else{
                onlineIcon.setVisibility(View.INVISIBLE);
            }
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_profile_picture_placeholder_female);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(user.getProfilePicURI())
                    .into(profilePicture);
            if(!seen){
                lastMsgTextView.setTypeface(ResourcesCompat.getFont(context, R.font.nunito_light), Typeface.BOLD);
            }

            if(type.equals("math")){
                lastMsgTextView.setText("Math Expression");
            } else if(type.equals("image")){
                lastMsgTextView.setText("Image Message");
            } else if(type.equals("location")){
                lastMsgTextView.setText("Location Message");
            } else{
                lastMsgTextView.setText(lastMessageContent);
            }

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopup(view, user.getId());
                    return false;
                }
            });
        }

        public void bind(Group group, String lastMessageContent, String type, long timestamp, boolean seen){
            nameTextView.setText("Group: " + group.getName());
            usernameTextView.setText("");
            String time = Utils.convertTimestampToDate(timestamp);
            dateTextView.setText(time);
//            if(user.isOnlineStatus()){
//                onlineIcon.setVisibility(View.VISIBLE);
//            }else{
//                onlineIcon.setVisibility(View.INVISIBLE);
//            }
            onlineIcon.setVisibility(View.INVISIBLE);
            //TODO group status online rakhne

            if(group.getGroupPicURI() != null){
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_profile_picture_placeholder_female);
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(group.getGroupPicURI())
                        .into(profilePicture);
            }else{
                TextDrawable letterDrawable = Utils.getTextDrawable(group.getName(), group.getId(), "round");
                profilePicture.setImageDrawable(letterDrawable);
            }
            if(!seen){
                Constants.CHATS_COUNT ++;
                lastMsgTextView.setTypeface(ResourcesCompat.getFont(context, R.font.nunito_light), Typeface.BOLD);
            }

            if(type.equals("math")){
                lastMsgTextView.setText("Math Expression");
            } else if(type.equals("image")){
                lastMsgTextView.setText("Image Message");
            } else if(type.equals("location")){
                lastMsgTextView.setText("Location Message");
            } else{
                lastMsgTextView.setText(lastMessageContent);
            }

        }

        public void showPopup(View view, final String uid) {
//        PopupMenu popup = new PopupMenu(this, v);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.chat_extensions_menu, popup.getMenu());

            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.chat_fragment_item_long_click_options, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.chat_fragment_open_profile:
                            Intent openProfileIntent = new Intent(context, UserProfileActivity.class);
                            openProfileIntent.putExtra("uid", uid);
                            context.startActivity(openProfileIntent);
                            break;
                        case R.id.chat_fragment_delete_convo:
                            deleteConvoFromFirebase(uid);
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }

        private void deleteConvoFromFirebase(String otheruseruid) {
            DatabaseReference dbRef = FirebaseDatabase
                    .getInstance()
                    .getReference();
//                    .child("chat")
//                    .child(FirebaseAuth.getInstance().getUid())
//                    .child(uid);
            String thisuserid = FirebaseAuth.getInstance().getUid();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("chat/" + thisuserid + "/" + otheruseruid, null);
            childUpdates.put("messages/" + thisuserid + "/" + otheruseruid, null);
            dbRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        ChatRecyclerAdapter.this.notifyDataSetChanged();
                        //Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}
