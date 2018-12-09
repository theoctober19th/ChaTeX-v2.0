package com.thecoffeecoders.chatex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
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
import com.google.firebase.database.ValueEventListener;
import com.thecoffeecoders.chatex.AddGroupActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.GroupRecyclerAdapter;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.chat.GroupChatActivity;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.utils.Utils;
import com.thecoffeecoders.chatex.views.RecyclerViewWithEmptyView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class GroupsFragment extends Fragment {

    ProgressBar mProgressBar;
    //RecyclerView mGroupListRecyclerView;
    RecyclerViewWithEmptyView mGroupListRecyclerView;
    FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;

    DatabaseReference mGroupRef;
    DatabaseReference mUserGroupRef;

    private OnFragmentInteractionListener mListener;
    private Context mContext;

    public GroupsFragment() {
        // Required empty public constructor
    }

    public static GroupsFragment newInstance(String param1, String param2) {
        GroupsFragment fragment = new GroupsFragment();
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
        View aView =  inflater.inflate(R.layout.fragment_groups, container, false);

        //Set Actionbar title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Groups");

        //setting up RecyclerView
        mGroupListRecyclerView = (RecyclerViewWithEmptyView) aView.findViewById(R.id.group_list_recyclerView);
        mGroupListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        View emptyView = aView.findViewById(R.id.fragment_group_empty_view);
        mGroupListRecyclerView.setEmptyView(emptyView);

        mProgressBar = aView.findViewById(R.id.fragment_group_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        mUserGroupRef = FirebaseDatabase.getInstance().getReference().child("usergroups").child(FirebaseAuth.getInstance().getUid());
        mUserGroupRef.keepSynced(true);
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");

        FloatingActionButton fab = aView.findViewById(R.id.add_group_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGroup();
            }
        });

        addFirebaseRecyclerAdapter();
        return aView;
    }

    private void addFirebaseRecyclerAdapter() {
        FirebaseRecyclerOptions<Boolean> options = new FirebaseRecyclerOptions.Builder<Boolean>()
                .setQuery(mUserGroupRef, Boolean.class)
                .build();
        mFirebaseRecyclerAdapter = new GroupRecyclerAdapter(options, mProgressBar);
        mGroupListRecyclerView.setAdapter(mFirebaseRecyclerAdapter);

    }

    private void takeToGroupChatActivity(String id) {
        Intent groupChatIntent = new Intent(mContext, GroupChatActivity.class);
        groupChatIntent.putExtra("groupid", id);
        startActivity(groupChatIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseRecyclerAdapter.startListening();
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
        mContext = context;
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

    public void addNewGroup(){
        Intent addGroupIntent = new Intent(getContext(), AddGroupActivity.class);
        startActivity(addGroupIntent);
    }


}
