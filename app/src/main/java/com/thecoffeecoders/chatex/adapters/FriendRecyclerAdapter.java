package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.users.UserProfileActivity;
import com.thecoffeecoders.chatex.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class FriendRecyclerAdapter extends FirebaseRecyclerAdapter<Friend, FriendRecyclerAdapter.FriendsViewHolder> {

    ProgressBar mProgressBar;

    public FriendRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, ProgressBar progressBar) {
        super(options);
        mProgressBar = progressBar;
    }

    @Override
    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friend model) {

        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query friendsQuery = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .orderByChild("onlineStatus");

        final String list_user_id = getRef(position).getKey();
        Log.d("bikalpa", list_user_id);
        mUserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String username = dataSnapshot.child("username").getValue().toString();
                String profilePicURI = dataSnapshot.child("profilePicURI").getValue().toString();
                String online = dataSnapshot.child("onlineStatus").getValue().toString();
                String displayName = dataSnapshot.child("displayName").getValue().toString();
                long lastOnline = (long)dataSnapshot.child("lastOnline").getValue();

                holder.setDisplayName(displayName);
                holder.setUserName(username);
                holder.setProfilePicture(profilePicURI);
                holder.setOnline(online);
                //holder.setLastOnlineDate(lastOnline);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //take user to UserProfileActivity
                        Intent chatIntent = new Intent(holder.context, ChatActivity.class);
                        chatIntent.putExtra("uid", list_user_id);
                        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        holder.context.startActivity(chatIntent);
                    }
                });
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        holder.showPopup(v, list_user_id);
                        return false;
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
                .inflate(R.layout.row_single_friend, viewGroup, false);

        return new FriendsViewHolder(view);
    }

    public void onDataChanged() {
        if (mProgressBar != null) {
            if (mProgressBar.getVisibility() == ProgressBar.VISIBLE) {
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private Context context;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            context = mView.getContext();
        }

        public void setLastOnlineDate(long timestamp){
            TextView lastOnlineTextView = (TextView)mView.findViewById(R.id.friend_single_lastOnline);
            lastOnlineTextView.setText(Utils.getElapsedTime(timestamp));
        }

        public void setDisplayName(String displayName){
            TextView nameTextView = (TextView)mView.findViewById(R.id.friend_single_name);
            nameTextView.setText(displayName);
        }

        public void setUserName(String userName){
            TextView usernameTextView = (TextView)mView.findViewById(R.id.friend_single_username);
            usernameTextView.setText("@" + userName);
        }

        public void setOnline(String online){
            CircularImageView onlineIcon = (CircularImageView)mView.findViewById(R.id.friend_single_online_icon);
            if (online.equals("true")){
                onlineIcon.setVisibility(View.VISIBLE);
            }else{
                onlineIcon.setVisibility(View.INVISIBLE);
            }
        }
        public void setProfilePicture(String profilePicURI){
            CircularImageView profilePicture = mView.findViewById(R.id.friend_single_image);

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_profile_picture_placeholder_female);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(profilePicURI)
                    .into(profilePicture);
        }

        public void showPopup(View view, final String uid) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.friend_fragment_item_long_click_options, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.friend_fragment_open_profile:
                            Intent openProfileIntent = new Intent(context, UserProfileActivity.class);
                            openProfileIntent.putExtra("uid", uid);
                            context.startActivity(openProfileIntent);
                            break;
                        case R.id.friend_fragment_unfriend:
                            deleteFriendFromFirebase(uid);
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }

        private void deleteFriendFromFirebase(String otheruserid) {
            DatabaseReference friendRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("friendlist");
            String thisuserid = FirebaseAuth.getInstance().getUid();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(thisuserid + "/" + otheruserid, null);
            childUpdates.put(otheruserid + "/" + thisuserid, null);

            friendRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        FriendRecyclerAdapter.this.notifyDataSetChanged();
                        Toast.makeText(context, "UNFRIENDED this person", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
