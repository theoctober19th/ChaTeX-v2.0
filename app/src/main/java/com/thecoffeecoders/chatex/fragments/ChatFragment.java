package com.thecoffeecoders.chatex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.ChatRecyclerAdapter;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.math.WriteEquationActivity;
import com.thecoffeecoders.chatex.misc.SendLocation;
import com.thecoffeecoders.chatex.models.Chat;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.users.UserProfileActivity;
import com.thecoffeecoders.chatex.views.RecyclerViewWithEmptyView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ChatFragment extends Fragment {
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Firebase Objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Query mMessagesRef;
    String mUserID;

//    DatabaseReference

    //Views and stuffs
    //RecyclerView mChatListRecyclerView;
    RecyclerViewWithEmptyView mChatListRecyclerView;
    ChatRecyclerAdapter mChatRecyclerAdapter;
    static ProgressBar mProgressBar;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    public void instantiateFirebaseObjects(){
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserID = mAuth.getUid();
        mMessagesRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("chat").child(mUserID)
                .orderByChild("seenTimestamp");
        mMessagesRef.keepSynced(true);
    }

    public void initializeViews(View parentView){
        mChatListRecyclerView = (RecyclerViewWithEmptyView) parentView.findViewById(R.id.chat_list_recyclerview);
        mChatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        View emptyView = parentView.findViewById(R.id.fragment_chat_empty_view);
        mChatListRecyclerView.setEmptyView(emptyView);
        mProgressBar = parentView.findViewById(R.id.fragment_chat_progress_bar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);

        //Set ActionBar title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Chats");

        instantiateFirebaseObjects();
        initializeViews(view);
        addAdapter();
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatRecyclerAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        mChatRecyclerAdapter.startListening();
    }

    public void addAdapter(){
        FirebaseRecyclerOptions<Chat> options = new FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(mMessagesRef, Chat.class)
                .build();
        mChatRecyclerAdapter = new ChatRecyclerAdapter(options, getContext(), mProgressBar);
        mChatListRecyclerView.setAdapter(mChatRecyclerAdapter);
    }

}
