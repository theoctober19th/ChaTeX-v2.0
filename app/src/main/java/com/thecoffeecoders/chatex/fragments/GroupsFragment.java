package com.thecoffeecoders.chatex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.chat.GroupChatActivity;
import com.thecoffeecoders.chatex.models.Friend;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class GroupsFragment extends Fragment {

    ProgressBar mProgressBar;
    RecyclerView mGroupListRecyclerView;
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

        mGroupListRecyclerView = aView.findViewById(R.id.group_list_recyclerView);
        mGroupListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mProgressBar = aView.findViewById(R.id.fragment_group_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        mUserGroupRef = FirebaseDatabase.getInstance().getReference().child("usergroups").child(FirebaseAuth.getInstance().getUid());
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

//    private void addFirebaseRecyclerAdapter() {
//        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
//                .setQuery(mGroupRef, Group.class)
//                .build();
//        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, GroupsFragment.GroupsViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull final GroupsFragment.GroupsViewHolder holder, int position, @NonNull final Group model) {
//                RequestOptions requestOptions = new RequestOptions()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .placeholder(R.drawable.img_cover_photo_placeholder);
//                if(model.getGroupPicURI()!=null){
//                    Log.d("grouppicuri", model.getGroupPicURI());
//                    Glide.with(mContext)
//                            .applyDefaultRequestOptions(requestOptions)
//                            .load(model.getGroupPicURI())
//                            .into(holder.mGroupPhoto);
//                }
//                if(model.getName() != null){
//                    holder.mGroupName.setText(model.getName());
//                }
//                holder.mMembersInfo.setText(model.getMemberCount() + " members");
//
//                Date date = new Date(Long.valueOf(model.getCreated()*1000L));
//                SimpleDateFormat myDate = new SimpleDateFormat("d MMM yyyy");
//                String formatted = myDate.format(date);
//                holder.mCreatedDate.setText("Created on " + formatted);
//
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        takeToGroupChatActivity(model.getId());
//                    }
//                });
//            }
//
//            @NonNull
//            @Override
//            public GroupsFragment.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//                View view = LayoutInflater.from(viewGroup.getContext())
//                        .inflate(R.layout.cardview_group_row, viewGroup, false);
//
//                return new GroupsFragment.GroupsViewHolder(view);
//            }
//        };
//    }

    private void addFirebaseRecyclerAdapter() {
        FirebaseRecyclerOptions<Boolean> options = new FirebaseRecyclerOptions.Builder<Boolean>()
                .setQuery(mUserGroupRef, Boolean.class)
                .build();
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Boolean, GroupsFragment.GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupsFragment.GroupsViewHolder holder, int position, @NonNull final Boolean bool) {

                String key = this.getRef(position).getKey();
                mGroupRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Group model = dataSnapshot.getValue(Group.class);

                        RequestOptions requestOptions = new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.img_cover_photo_placeholder);
                        if(model.getGroupPicURI()!=null){
                            Log.d("grouppicuri", model.getGroupPicURI());
                            Glide.with(mContext)
                                    .applyDefaultRequestOptions(requestOptions)
                                    .load(model.getGroupPicURI())
                                    .into(holder.mGroupPhoto);
                        }else{
                            TextDrawable letterDrawable = Utils.getTextDrawable(model.getName(), model.getId(), "rectangle");
                            holder.mGroupPhoto.setImageDrawable(letterDrawable);
                        }
                        if(model.getName() != null){
                            holder.mGroupName.setText(model.getName());
                        }
                        holder.mMembersInfo.setText(model.getMemberCount() + " members");

                        Date date = new Date(Long.valueOf(model.getCreated()*1000L));
                        SimpleDateFormat myDate = new SimpleDateFormat("d MMM yyyy");
                        String formatted = myDate.format(date);
                        holder.mCreatedDate.setText("Created on " + formatted);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                takeToGroupChatActivity(model.getId());
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
            public GroupsFragment.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.cardview_group_row, viewGroup, false);

                return new GroupsFragment.GroupsViewHolder(view);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull GroupsViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
        };
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
        addFirebaseRecyclerAdapter();
        mGroupListRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
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

    public class GroupsViewHolder extends RecyclerView.ViewHolder{

        ImageView mGroupPhoto;
        TextView mGroupName;
        TextView mCreatedDate;
        TextView mMembersInfo;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);

            mGroupPhoto = itemView.findViewById(R.id.group_row_group_photo);
            mGroupName = itemView.findViewById(R.id.group_row_group_name);
            mCreatedDate = itemView.findViewById(R.id.group_row_created_date);
            mMembersInfo = itemView.findViewById(R.id.group_row_member_info);
        }
    }
}
