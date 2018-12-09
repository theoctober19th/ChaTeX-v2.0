package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
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
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.fragments.GroupsFragment;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupRecyclerAdapter extends FirebaseRecyclerAdapter<Boolean, GroupRecyclerAdapter.GroupsViewHolder> {

    private ProgressBar mProgressBar;

    public GroupRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Boolean> options, ProgressBar progressBar) {
        super(options);
        mProgressBar = progressBar;
    }

    protected void onBindViewHolder(@NonNull final GroupRecyclerAdapter.GroupsViewHolder holder, int position, @NonNull final Boolean bool) {

        String key = this.getRef(position).getKey();
        DatabaseReference mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        mGroupRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Group model = dataSnapshot.getValue(Group.class);
                holder.bind(model);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @NonNull
    @Override
    public GroupRecyclerAdapter.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_group_row, viewGroup, false);

        return new GroupRecyclerAdapter.GroupsViewHolder(view);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull GroupsViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        mProgressBar.setVisibility(ProgressBar.GONE);
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder{

        ImageView mGroupPhoto;
        TextView mGroupName;
        TextView mCreatedDate;
        TextView mMembersInfo;
        Context mContext;
        View mView;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            mGroupPhoto = itemView.findViewById(R.id.group_row_group_photo);
            mGroupName = itemView.findViewById(R.id.group_row_group_name);
            mCreatedDate = itemView.findViewById(R.id.group_row_created_date);
            mMembersInfo = itemView.findViewById(R.id.group_row_member_info);
            mContext = itemView.getContext();
        }

        public void bind(final Group model){
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_cover_photo_placeholder);
            if(model.getGroupPicURI()!=null){
                Log.d("grouppicuri", model.getGroupPicURI());
                Glide.with(mContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(model.getGroupPicURI())
                        .into(mGroupPhoto);
            }else{
                TextDrawable letterDrawable = Utils.getTextDrawable(model.getName(), model.getId(), "rectangle");
                mGroupPhoto.setImageDrawable(letterDrawable);
            }
            if(model.getName() != null){
                mGroupName.setText(model.getName());
            }
            mMembersInfo.setText(model.getMemberCount() + " members");

            Date date = new Date(Long.valueOf(model.getCreated()));
            SimpleDateFormat myDate = new SimpleDateFormat("dd MMM yyyy");
            String formatted = myDate.format(date);
            mCreatedDate.setText("Created on " + formatted);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeToGroupChatActivity(model.getId());
                }
            });
        }

        public void takeToGroupChatActivity(String groupID){

        }
    }
}

