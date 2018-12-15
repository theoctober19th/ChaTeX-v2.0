package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import com.thecoffeecoders.chatex.ChatexChatApplication;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.ViewEquation;
import com.thecoffeecoders.chatex.misc.SendLocation;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.utils.Utils;

import java.io.IOException;

import katex.hourglass.in.mathlib.MathView;

//import io.github.kexanie.library.MathView;

public class MessageRecyclerAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    final int VIEW_TYPE_SENDER_TEXT = 1;
    final int VIEW_TYPE_RECEIVER_TEXT = 2;
    final int VIEW_TYPE_SENDER_IMAGE = 3;
    final int VIEW_TYPE_RECEIVER_IMAGE = 4;
    final int VIEW_TYPE_SENDER = 5;
    final int VIEW_TYPE_RECEIVER = 6;

    String thisUserID;
    String otherUserID;
    String otherUserProfilePicURI;

    public MessageRecyclerAdapter(@NonNull FirebaseRecyclerOptions options, String thisUserID, String otherUserID/*, String otherUserProfilePicURI*/) {
        super(options);
        this.thisUserID =thisUserID;
        this.otherUserID = otherUserID;

        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users").child(otherUserID).child("profilePicURI");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    otherUserProfilePicURI = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        String senderUID = this.getItem(position).getSender();
        String type = this.getItem(position).getType();

//        if(senderUID.equals(thisUserID) && type.equals("text")){
//            return VIEW_TYPE_SENDER_TEXT;
//        }else if(senderUID.equals(thisUserID) && type.equals("image")){
//            return VIEW_TYPE_SENDER_IMAGE;
//        }else if(senderUID.equals(otherUserID) && type.equals("text")){
//            return VIEW_TYPE_RECEIVER_TEXT;
//        }else if(senderUID.equals(otherUserID) && type.equals("image")){
//            return VIEW_TYPE_RECEIVER_IMAGE;
//        }
        if(senderUID.equals(thisUserID)){
            return VIEW_TYPE_SENDER;
        }else{
            return VIEW_TYPE_RECEIVER;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position, @NonNull final Message message) {

        String senderUID = message.getSender();
        switch (holder.getItemViewType()){
            case VIEW_TYPE_SENDER:
                ((SentMessageHolder)holder).bind(message, false);
                break;
            case VIEW_TYPE_RECEIVER:
                if(otherUserProfilePicURI == null){
                    DatabaseReference userRef = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("users").child(senderUID).child("profilePicURI");
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String profilePicURI = dataSnapshot.getValue().toString();
                                otherUserProfilePicURI = profilePicURI;
                                boolean adjascency = false;
                                if(position > 0){
                                    if(getItemViewType(position-1) == VIEW_TYPE_RECEIVER){
                                        adjascency = true;
                                    }
                                }
                                ((ReceivedMessageHolder)holder).bind(message, profilePicURI, adjascency);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }else{
                    boolean isAdjascent = false;
                    if(position > 0){
                        if(getItemViewType(position-1) == VIEW_TYPE_RECEIVER){
                            isAdjascent = true;
                        }
                    }
                    ((ReceivedMessageHolder)holder).bind(message, otherUserProfilePicURI, isAdjascent);
                }
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
        ImageView imageMessageImgView;
        io.github.kexanie.library.MathView mathView;
        Context context;
        ProgressBar progressBar;
        ScrollView mathContainer;
        View mView;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            profileImageThumb = mView.findViewById(R.id.user_single_profilepic_other);
            textMessage = mView.findViewById(R.id.user_single_textmessage_other);
            receivedTime = mView.findViewById(R.id.user_single_time_other);
            imageMessageImgView = mView.findViewById(R.id.user_single_imagemessage_other);
            mathContainer = mView.findViewById(R.id.mathview_container_other);
            mathView = mView.findViewById(R.id.mathmessage_other);
        }

        private void bind(final Message message, String profilePicURI, boolean adjascency){
            context = mView.getContext();

            String messageType = message.getType();
            String content = message.getContent();
            if(messageType.equals("text")){
                textMessage.setText(content);
                textMessage.setVisibility(View.VISIBLE);
            }else if(messageType.equals("image")){

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(content)
                        .into(imageMessageImgView);

                imageMessageImgView.setVisibility(View.VISIBLE);

            }else if(messageType.equals("math")){
                mathView.setText("\\(" + message.getContent() + "\\)");

                mathContainer.setVisibility(View.VISIBLE);
                mathView.setVisibility(View.VISIBLE);
                mathView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Intent viewEquationIntent = new Intent(context, ViewEquation.class);
                        viewEquationIntent.putExtra("equation", message.getContent());
                        context.startActivity(viewEquationIntent);
                        return false;
                    }
                });

            }else if(message.getType().equals("location")){
                textMessage.setText("Location Message");
                textMessage.setTextColor(Color.BLUE);
                textMessage.setVisibility(View.VISIBLE);
                textMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewMapIntent = new Intent(context, SendLocation.class);
                        viewMapIntent.putExtra("location", message.getContent());
                        context.startActivity(viewMapIntent);
                    }
                });
            }

            if(!adjascency){
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(profilePicURI)
                        .into(profileImageThumb);
            }
            String recievedTime = Utils.convertTimestampToDate(message.getTimestamp());
            receivedTime.setText(recievedTime);
        }
    }


    public static class SentMessageHolder extends RecyclerView.ViewHolder{

        TextView textMessage;
        TextView sentTime;
        ProgressBar progressBar;
        ImageView imageMessage;
        io.github.kexanie.library.MathView mathView;
        ScrollView mathContainer;
        Context context;
        View mView;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            mView = itemView;

            textMessage = itemView.findViewById(R.id.user_single_textmessage_thisuser);
            sentTime = itemView.findViewById(R.id.user_single_time_thisuser);
            progressBar = mView.findViewById(R.id.sent_message_progress_bar);
            imageMessage = itemView.findViewById(R.id.user_single_imagemessage_this);
            mathView = itemView.findViewById(R.id.mathmessage_this);
            mathContainer = itemView.findViewById(R.id.mathview_container_this);
        }

        private void bind(final Message message, boolean adjascency){
            if(message.getType().equals("text")){
                textMessage.setText(message.getContent());
                textMessage.setVisibility(View.VISIBLE);
            }else if(message.getType().equals("image")){
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(message.getContent())
                        .into(imageMessage);
                imageMessage.setVisibility(View.VISIBLE);
            }else if(message.getType().equals("math")){

                mathView.setText("\\(" + message.getContent() + "\\)");

                mathContainer.setVisibility(View.VISIBLE);
                mathView.setVisibility(View.VISIBLE);
                mathView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Intent viewEquationIntent = new Intent(context, ViewEquation.class);
                        viewEquationIntent.putExtra("equation", message.getContent());
                        context.startActivity(viewEquationIntent);
                        return false;
                    }
                });

            }else if(message.getType().equals("location")){
                textMessage.setText("Location Message");
                textMessage.setTextColor(Color.BLUE);
                textMessage.setVisibility(View.VISIBLE);
                textMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewMapIntent = new Intent(context, SendLocation.class);
                        viewMapIntent.putExtra("location", message.getContent());
                        context.startActivity(viewMapIntent);
                    }
                });
            }
            String time = Utils.convertTimestampToDate(message.getTimestamp());
            sentTime.setText(time);
            progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}



//public class MessageRecyclerAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {
//
//    final int VIEW_TYPE_SENDER = 1;
//    final int VIEW_TYPE_RECEIVER = 2;
//
//    String thisUserID;
//    String otherUserID;
//    String otherUserProfilePicURI;
//
//    public MessageRecyclerAdapter(@NonNull FirebaseRecyclerOptions options, String thisUserID, String otherUserID/*, String otherUserProfilePicURI*/) {
//        super(options);
//        this.thisUserID =thisUserID;
//        this.otherUserID = otherUserID;
//
//        DatabaseReference userRef = FirebaseDatabase
//                .getInstance()
//                .getReference()
//                .child("users").child(otherUserID).child("profilePicURI");
//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    otherUserProfilePicURI = dataSnapshot.getValue().toString();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        String senderUID = this.getItem(position).getSender();
//        if(senderUID.equals(FirebaseAuth.getInstance().getUid())){
//            return VIEW_TYPE_SENDER;
//        }else{
//            return VIEW_TYPE_RECEIVER;
//        }
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position, @NonNull final Message message) {
//
//        String senderUID = message.getSender();
//        switch (holder.getItemViewType()){
//            case VIEW_TYPE_SENDER:
//                boolean adjascency = false;
//                if(position > 0){
//                    if(getItemViewType(position-1) == VIEW_TYPE_SENDER){
//                        adjascency = true;
//                    }
//                }
//                ((SentMessageHolder)holder).bind(message, adjascency);
//                break;
//            case VIEW_TYPE_RECEIVER:
//                if(otherUserProfilePicURI == null){
//                    DatabaseReference userRef = FirebaseDatabase
//                            .getInstance()
//                            .getReference()
//                            .child("users").child(senderUID).child("profilePicURI");
//                    userRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if(dataSnapshot.exists()){
//                                String profilePicURI = dataSnapshot.getValue().toString();
//                                otherUserProfilePicURI = profilePicURI;
//                                boolean adjascency = false;
//                                if(position > 0){
//                                    if(getItemViewType(position-1) == VIEW_TYPE_RECEIVER){
//                                        adjascency = true;
//                                    }
//                                }
//                                ((ReceivedMessageHolder)holder).bind(message, profilePicURI, adjascency);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                        }
//                    });
//                }else{
//                    boolean isAdjascent = false;
//                    if(position > 0){
//                        if(getItemViewType(position-1) == VIEW_TYPE_RECEIVER){
//                            isAdjascent = true;
//                        }
//                    }
//                    ((ReceivedMessageHolder)holder).bind(message, otherUserProfilePicURI, isAdjascent);
//                }
//                break;
//        }
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//        View view;
//
//        if(viewType == VIEW_TYPE_SENDER){
//            view = LayoutInflater.from(viewGroup.getContext())
//                    .inflate(R.layout.row_single_message_sent, viewGroup, false);
//            return new SentMessageHolder(view);
//        }else if(viewType == VIEW_TYPE_RECEIVER){
//            view = LayoutInflater.from(viewGroup.getContext())
//                    .inflate(R.layout.row_single_message_received, viewGroup, false);
//            return new ReceivedMessageHolder(view);
//        }
//        return null;
//    }
//
//    public static class ReceivedMessageHolder extends RecyclerView.ViewHolder{
//
//        CircularImageView profileImageThumb;
//        TextView textMessage;
//        TextView receivedTime;
//        ImageView imageMessageImgView;
//        MathView mathView;
//        Context context;
//        ProgressBar progressBar;
//        ScrollView mathContainer;
//        View mView;
//
//        public ReceivedMessageHolder(@NonNull View itemView) {
//            super(itemView);
//            mView = itemView;
//
//            profileImageThumb = mView.findViewById(R.id.user_single_profilepic_other);
//            textMessage = mView.findViewById(R.id.user_single_textmessage_other);
//            receivedTime = mView.findViewById(R.id.user_single_time_other);
//            imageMessageImgView = mView.findViewById(R.id.user_single_imagemessage_other);
//            mathContainer = mView.findViewById(R.id.mathview_container_other);
//            mathView = mView.findViewById(R.id.mathmessage_other);
//        }
//
//        private void bind(final Message message, String profilePicURI, boolean adjascency){
//            context = mView.getContext();
//
//            String messageType = message.getType();
//            String content = message.getContent();
//            if(messageType.equals("text")){
//                textMessage.setText(content);
//                textMessage.setVisibility(View.VISIBLE);
//            }else if(messageType.equals("image")){
//
//                RequestOptions requestOptions = new RequestOptions()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                Glide.with(context)
//                        .applyDefaultRequestOptions(requestOptions)
//                        .load(content)
//                        .into(imageMessageImgView);
//
//                imageMessageImgView.setVisibility(View.VISIBLE);
//
//            }else if(messageType.equals("math")){
////                mathView.setDisplayText("\\(" + content + "\\)");
////                mathView.setVisibility(View.VISIBLE);
////                mathContainer.setVisibility(View.VISIBLE);
//
//                String latexurl = "https://latex.codecogs.com/png.latex?" + content;
//                RequestOptions requestOptions = new RequestOptions()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                Glide.with(context)
//                        .applyDefaultRequestOptions(requestOptions)
//                        .load(latexurl)
//                        .into(imageMessageImgView);
//
//                imageMessageImgView.setVisibility(View.VISIBLE);
//
//            }else if(message.getType().equals("location")){
//                textMessage.setText("Location Message");
//                textMessage.setTextColor(Color.BLUE);
//                textMessage.setVisibility(View.VISIBLE);
//                textMessage.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent viewMapIntent = new Intent(context, SendLocation.class);
//                        viewMapIntent.putExtra("location", message.getContent());
//                        context.startActivity(viewMapIntent);
//                    }
//                });
//            }
//
//            if(!adjascency){
//                RequestOptions requestOptions = new RequestOptions()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                Glide.with(context)
//                        .applyDefaultRequestOptions(requestOptions)
//                        .load(profilePicURI)
//                        .into(profileImageThumb);
//            }
//            String recievedTime = Utils.convertTimestampToDate(message.getTimestamp());
//            receivedTime.setText(recievedTime);
//        }
//    }
//
//
//    public static class SentMessageHolder extends RecyclerView.ViewHolder{
//
//        TextView textMessage;
//        TextView sentTime;
//        ProgressBar progressBar;
//        ImageView imageMessage;
//        io.github.kexanie.library.MathView mathView;
//        ScrollView mathContainer;
//        Context context;
//        View mView;
//
//        public SentMessageHolder(@NonNull View itemView) {
//            super(itemView);
//            context = itemView.getContext();
//            mView = itemView;
//
//            textMessage = itemView.findViewById(R.id.user_single_textmessage_thisuser);
//            sentTime = itemView.findViewById(R.id.user_single_time_thisuser);
//            progressBar = mView.findViewById(R.id.sent_message_progress_bar);
//            imageMessage = itemView.findViewById(R.id.user_single_imagemessage_this);
//            mathView = itemView.findViewById(R.id.mathmessage_this);
//            mathContainer = itemView.findViewById(R.id.mathview_container_this);
//        }
//
//        private void bind(final Message message, boolean adjascency){
//            if(message.getType().equals("text")){
//                textMessage.setText(message.getContent());
//                textMessage.setVisibility(View.VISIBLE);
//            }else if(message.getType().equals("image")){
//                RequestOptions requestOptions = new RequestOptions()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL);
//                Glide.with(context)
//                        .applyDefaultRequestOptions(requestOptions)
//                        .load(message.getContent())
//                        .into(imageMessage);
//                imageMessage.setVisibility(View.VISIBLE);
//            }else if(message.getType().equals("math")){
//
//                mathView.setText("\\(" + message.getContent() + "\\)");
//
//                mathContainer.setVisibility(View.VISIBLE);
//                mathView.setVisibility(View.VISIBLE);
//            }else if(message.getType().equals("location")){
//                textMessage.setText("Location Message");
//                textMessage.setTextColor(Color.BLUE);
//                textMessage.setVisibility(View.VISIBLE);
//                textMessage.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent viewMapIntent = new Intent(context, SendLocation.class);
//                        viewMapIntent.putExtra("location", message.getContent());
//                        context.startActivity(viewMapIntent);
//                    }
//                });
//            }
//            String time = Utils.convertTimestampToDate(message.getTimestamp());
//            sentTime.setText(time);
//            progressBar.setVisibility(ProgressBar.GONE);
//        }
//    }
//}
