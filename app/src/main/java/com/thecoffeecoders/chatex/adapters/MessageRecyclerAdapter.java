package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.utils.Utils;

public class MessageRecyclerAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    final int VIEW_TYPE_SENDER = 1;
    final int VIEW_TYPE_RECEIVER = 2;

    public MessageRecyclerAdapter(@NonNull FirebaseRecyclerOptions options) {
        super(options);
    }

    @Override
    public int getItemViewType(int position) {
        String senderUID = this.getItem(position).getSender();
        if(senderUID.equals(FirebaseAuth.getInstance().getUid())){
            return VIEW_TYPE_SENDER;
        }else{
            return VIEW_TYPE_RECEIVER;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position, @NonNull final Message message) {

        String senderUID = message.getSender();
        switch (holder.getItemViewType()){
            case VIEW_TYPE_SENDER:
                ((SentMessageHolder)holder).bind(message);
                break;
            case VIEW_TYPE_RECEIVER:
                DatabaseReference userRef = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("users").child(senderUID).child("profilePicURI");
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String profilePicURI = dataSnapshot.getValue().toString();
                            ((ReceivedMessageHolder)holder).bind(message, profilePicURI);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                break;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;

        if(viewType == VIEW_TYPE_SENDER){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_single_message_sent, viewGroup, false);
            return new SentMessageHolder(view);
        }else if(viewType == VIEW_TYPE_RECEIVER){
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_single_message_received, viewGroup, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    public static class ReceivedMessageHolder extends RecyclerView.ViewHolder{

        CircularImageView profileImageThumb;
        TextView textMessage;
        TextView receivedTime;
        Context context;
        View mView;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            profileImageThumb = mView.findViewById(R.id.user_single_profilepic_other);
            textMessage = mView.findViewById(R.id.user_single_textmessage_other);
            receivedTime = mView.findViewById(R.id.user_single_time_other);
        }

        private void bind(Message message, String profilePicURI){
            context = mView.getContext();

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_cover_photo_placeholder);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(profilePicURI)
                    .into(profileImageThumb);
            textMessage.setText(message.getContent());
            String recievedTime = Utils.convertTimestampToDate(message.getTimestamp());
            receivedTime.setText(recievedTime);
        }

    }


    public static class SentMessageHolder extends RecyclerView.ViewHolder{

        TextView textMessage;
        TextView sentTime;
        Context context;
        View mView;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            mView = itemView;

            textMessage = itemView.findViewById(R.id.user_single_textmessage_thisuser);
            sentTime = itemView.findViewById(R.id.user_single_time_thisuser);
        }

        private void bind(Message message){
            textMessage.setText(message.getContent());
            String time = Utils.convertTimestampToDate(message.getTimestamp());
            sentTime.setText(time);
        }
    }
}
