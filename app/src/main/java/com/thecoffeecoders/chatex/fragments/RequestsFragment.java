package com.thecoffeecoders.chatex.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.RequestRecyclerAdapter;
import com.thecoffeecoders.chatex.models.Chat;
import com.thecoffeecoders.chatex.models.Request;
import com.thecoffeecoders.chatex.views.RecyclerViewWithEmptyView;

public class RequestsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    RecyclerViewWithEmptyView mRequestRecyclerView;
    RequestRecyclerAdapter mRequestRecyclerAdapter;

    DatabaseReference mRequestsRef;

    public RequestsFragment() {
        // Required empty public constructor
    }


    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_requests, container, false);
        //Set Actionbar title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Requests");

        initializeViews(view);
        initializeFirebase();

        return view;
    }

    private void initializeViews(View view) {
        mRequestRecyclerView = view.findViewById(R.id.request_recycler_view);
        View emptyView = view.findViewById(R.id.fragment_requests_empty_view);
        mRequestRecyclerView.setEmptyView(emptyView);
        mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    private void initializeFirebase(){
        mRequestsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("requests")
                .child(FirebaseAuth.getInstance().getUid());
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
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(mRequestsRef, Request.class)
                .build();
        mRequestRecyclerAdapter = new RequestRecyclerAdapter(options, getContext());
        mRequestRecyclerView.setAdapter(mRequestRecyclerAdapter);
        mRequestRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRequestRecyclerAdapter.stopListening();
    }
}
