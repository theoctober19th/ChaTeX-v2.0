package com.thecoffeecoders.chatex.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.interfaces.OnAdapterItemClicked;
import com.thecoffeecoders.chatex.models.Friend;

import java.util.HashMap;
import java.util.Map;

public class AddGroupFriendAdapter extends FirebaseRecyclerAdapter<Friend, AddGroupFriendAdapter.FriendViewHolder> {

    Map<String, Object> memberInfoMap = new HashMap<>();
    Map<String, Object> membersMap = new HashMap<>();

    private OnAdapterItemClicked mCallBack;

    private static int USER_COUNT = 1;

    public AddGroupFriendAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, OnAdapterItemClicked listener) {
        super(options);

        memberInfoMap.put("joined", System.currentTimeMillis());
        memberInfoMap.put("nickname", "");
        this.mCallBack = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull final AddGroupFriendAdapter.FriendViewHolder holder, int position, @NonNull Friend model) {
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
                            if(holder.mCheckBox.isChecked()){
                                holder.mCheckBox.setChecked(false);
                            }else{
                                holder.mCheckBox.setChecked(true);
                            }
                        }
                    });
                    holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            mCallBack.onAdapterItemClicked(list_user_id);
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
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_single_friend_add_group, viewGroup, false);
        return new FriendViewHolder(view);
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder{

        CircularImageView mProfilePicture;
        TextView mDisplayName;
        TextView mUsername;
        CheckBox mCheckBox;
        CircularImageView mOnlineIcon;
        View mView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            mProfilePicture = itemView.findViewById(R.id.add_group_friend_single_image);
            mDisplayName = itemView.findViewById(R.id.add_group_friend_single_name);
            mUsername = itemView.findViewById(R.id.add_group_friend_single_username);
            mOnlineIcon = itemView.findViewById(R.id.add_group_friend_single_online_icon);
            mCheckBox = itemView.findViewById(R.id.add_group_friend_single_checkbox);
        }

        public void bind(String profilePicURI, String name, String username, String onlineStatus){
            mDisplayName.setText(name);
            mUsername.setText("@" + username);

            Glide.with(mView.getContext().getApplicationContext())
                    .applyDefaultRequestOptions(
                            new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.img_profile_picture_placeholder_female)
                    ).load(profilePicURI)
                    .into(mProfilePicture);

            mOnlineIcon.setVisibility( onlineStatus.equals("true") ? View.VISIBLE : View.INVISIBLE );
            mCheckBox.setChecked(false);
        }
    }
}
