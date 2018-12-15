package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.interfaces.OnAdapterItemClicked;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.users.UserProfileActivity;

import java.util.HashMap;
import java.util.Map;

public class GroupMemberAdapter extends FirebaseRecyclerAdapter<Boolean, GroupMemberAdapter.GroupMemberViewHolder> {

    private OnAdapterItemClicked mCallBack;
    private String mGroupID;

    private static int USER_COUNT = 1;

    public GroupMemberAdapter(@NonNull FirebaseRecyclerOptions<Boolean> options, OnAdapterItemClicked listener) {
        super(options);
        this.mCallBack = listener;
    }

    public GroupMemberAdapter(@NonNull FirebaseRecyclerOptions<Boolean> options, String groupID) {
        super(options);
        this.mCallBack = null;
        this.mGroupID = groupID;
    }

    @Override
    protected void onBindViewHolder(@NonNull final GroupMemberAdapter.GroupMemberViewHolder holder, int position, @NonNull Boolean model) {
        final String list_user_id = getRef(position).getKey();

        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        mUserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String username = dataSnapshot.child("username").getValue().toString();
                    String profilePicURI = dataSnapshot.child("profilePicURI").getValue().toString();
                    String online = dataSnapshot.child("onlineStatus").getValue().toString();
                    String displayName = dataSnapshot.child("displayName").getValue().toString();

                    holder.bind(profilePicURI, displayName, username, online);

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.showPopup(v, list_user_id);
                        }
                    });
//                    holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                            mCallBack.onAdapterItemClicked(list_user_id);
//                        }
//                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @NonNull
    @Override
    public GroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_single_group_member, viewGroup, false);
        return new GroupMemberViewHolder(view);
    }

    public class GroupMemberViewHolder extends RecyclerView.ViewHolder{

        CircularImageView mProfilePicture;
        TextView mDisplayName;
        CircularImageView mOnlineIcon;
        Context viewContext;
        View mView;

        public GroupMemberViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            mProfilePicture = itemView.findViewById(R.id.group_member_single_image);
            mDisplayName = itemView.findViewById(R.id.group_member_single_name);
            mOnlineIcon = itemView.findViewById(R.id.group_member_single_online_icon);
            viewContext = itemView.getContext();
        }

        public void bind(String profilePicURI, String name, String username, String onlineStatus){
            mDisplayName.setText(name);

            Glide.with(mView.getContext().getApplicationContext())
                    .applyDefaultRequestOptions(
                            new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.img_profile_picture_placeholder_female)
                    ).load(profilePicURI)
                    .into(mProfilePicture);

            mOnlineIcon.setVisibility( onlineStatus.equals("true") ? View.VISIBLE : View.INVISIBLE );
        }

        public void showPopup(View view, final String uid) {
//        PopupMenu popup = new PopupMenu(this, v);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.chat_extensions_menu, popup.getMenu());

            PopupMenu popup = new PopupMenu(viewContext, view);
            popup.getMenuInflater().inflate(R.menu.group_member_click_options, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.group_member_remove_from_group:
                            removeUserFromGroup(uid);
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }

        public void removeUserFromGroup(String uid){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("groupmembers/" + mGroupID + "/" + uid, null);
            childUpdates.put("usergroups/" + uid + "/" + mGroupID, null);

            dbRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //successful do something here
                        GroupMemberAdapter.this.notifyDataSetChanged();
                    }
                }
            });
        }

    }
}
