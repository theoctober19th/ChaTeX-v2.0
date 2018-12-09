package com.thecoffeecoders.chatex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.adapters.FriendRecyclerAdapter;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.views.RecyclerViewWithEmptyView;

public class FriendsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    //Firebase objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mFriendsRef;
    DatabaseReference mUserRef;

    private View mMainView;
    ProgressBar mProgressBar;
    //RecyclerView
    private RecyclerViewWithEmptyView mFriendList;
    FriendRecyclerAdapter mFriendRecyclerAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        //Set Actionbar title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Friends");

        //Firebase objects
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Query query = FirebaseDatabase.getInstance().getReference().child("friendlist");

        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("friendlist").child(mUser.getUid());
        mFriendsRef.keepSynced(true);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        mUserRef.keepSynced(true);

        //RecyclerView instantiation
        mFriendList = (RecyclerViewWithEmptyView) mMainView.findViewById(R.id.friends_list_recycler_view);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        View emptyView = mMainView.findViewById(R.id.fragment_friends_empty_view);
        mFriendList.setEmptyView(emptyView);

        return mMainView;

    }

    // TODO: Rename method, update argument and hook method into UI event
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
    public void onStart() {
        super.onStart();

        //FirebaseRecyclerAdapter
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(mFriendsRef, Friend.class)
                .build();

        mFriendRecyclerAdapter = new FriendRecyclerAdapter(options, mProgressBar);
        mFriendList.setAdapter(mFriendRecyclerAdapter);
        mFriendRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFriendRecyclerAdapter.stopListening();
    }
}
