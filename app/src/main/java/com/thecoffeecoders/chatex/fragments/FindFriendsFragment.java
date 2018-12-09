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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.users.UserProfileActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FindFriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FindFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindFriendsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Firebase objects
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUsersRef;

    private View mMainView;
    //RecyclerView
    private RecyclerView mFindFriendRecyclerView;
    //Recycler Adapter
    FirebaseRecyclerAdapter findFriendsRecyclerAdapter;

    public FindFriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FindFriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FindFriendsFragment newInstance(String param1, String param2) {
        FindFriendsFragment fragment = new FindFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_find_friends, container, false);

        //Set Actionbar title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("User List");

        //Firebase objects
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mUsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersRef.keepSynced(true);

        //RecyclerView instantiation
        mFindFriendRecyclerView = (RecyclerView)mMainView.findViewById(R.id.find_friends_recyclerview);
       // mFindFriendRecyclerView.setHasFixedSize(true);
        mFindFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDisplayName(String displayName){
            TextView nameTextView = (TextView)mView.findViewById(R.id.user_single_name);
            nameTextView.setText(displayName);
        }

        public void setBio(String bio){
            TextView dateTextView = (TextView)mView.findViewById(R.id.user_single_bio);
            dateTextView.setText(bio);
        }

        public void setUserName(String userName){
            TextView usernameTextView = (TextView)mView.findViewById(R.id.user_single_username);
            usernameTextView.setText("@" + userName);
        }

        public void setOnline(boolean online){
            CircularImageView onlineIcon = (CircularImageView)mView.findViewById(R.id.user_single_online_icon);
            if (online){
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
            Glide.with(getContext())
                    .applyDefaultRequestOptions(requestOptions)
                    .load(profilePicURI)
                    .into(profilePicture);
        }
    }


    public void onStart() {
        super.onStart();

        //FirebaseRecyclerAdapter
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(mUsersRef, User.class)
                .build();

        findFriendsRecyclerAdapter = new FirebaseRecyclerAdapter<User, FindFriendsFragment.UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FindFriendsFragment.UsersViewHolder holder, int position, @NonNull User model) {
                holder.setBio(model.getBio());
                holder.setDisplayName(model.getDisplayName());
                holder.setUserName(model.getUsername());
                holder.setProfilePicture(model.getProfilePicURI());
                holder.setOnline(model.isOnlineStatus());
                final String modelID = model.getId();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userProfileIntent = new Intent(getContext(), UserProfileActivity.class);
                        userProfileIntent.putExtra("uid", modelID);
                        startActivity(userProfileIntent);
                    }
                });
            }


            @NonNull
            @Override
            public FindFriendsFragment.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.user_single_layout, viewGroup, false);

                return new FindFriendsFragment.UsersViewHolder(view);
            }
        };
        mFindFriendRecyclerView.setAdapter(findFriendsRecyclerAdapter);
        findFriendsRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        findFriendsRecyclerAdapter.stopListening();
    }
}
