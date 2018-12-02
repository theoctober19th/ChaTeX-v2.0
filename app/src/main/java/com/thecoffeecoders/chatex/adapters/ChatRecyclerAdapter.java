package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.models.Chat;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.utils.Utils;

public class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<Chat, RecyclerView.ViewHolder> {

    Context mContext;
    ProgressBar mProgressBar;

    public ChatRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Chat> options, Context context, ProgressBar progressBar) {
        super(options);
        mContext = context;
        mProgressBar = progressBar;
    }

    @Override
    protected void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position, @NonNull final Chat model) {
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
                                final long timestamp = message.getTimestamp();
                                ((ChatViewHolder)holder).bind(user, content, timestamp, model.isSeen());
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

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

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

        public void bind(User user, String lastMessageContent, long timestamp, boolean seen){
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
            lastMsgTextView.setText(lastMessageContent);
        }
    }
}
