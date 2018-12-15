package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.thecoffeecoders.chatex.chat.GroupChatActivity;
import com.thecoffeecoders.chatex.models.Group;
import com.thecoffeecoders.chatex.models.Message;
import com.thecoffeecoders.chatex.models.Request;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.utils.Utils;

public class RequestRecyclerAdapter extends FirebaseRecyclerAdapter<Request, RequestRecyclerAdapter.RequestViewHolder> {

    Context mContext;

    public RequestRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Request> options, Context context) {
        super(options);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull final Request model) {
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

                    holder.bind(user, model.getType(), model.getTimestamp());
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //take user to UserProfileActivity
                            Intent chatIntent = new Intent(v.getContext(), ChatActivity.class);
                            chatIntent.putExtra("uid", uid);
                            mContext.startActivity(chatIntent);
                        }
                    });
                    holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            //TODO something
                            Toast.makeText(mContext, "LongClick", Toast.LENGTH_SHORT).show();
                            return false;
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
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_single_request, viewGroup, false);
        return new RequestViewHolder(view);
    }

    public class RequestViewHolder extends RecyclerView

            .ViewHolder{
        Context viewHolderContext;
        TextView nameTextView;
        TextView typeTextView;
        TextView usernameTextView;
        CircularImageView profilePicture;
        TextView dateTextView;
        View mView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            viewHolderContext = itemView.getContext();
            mView = itemView;

            nameTextView = itemView.findViewById(R.id.request_single_name);
            usernameTextView = itemView.findViewById(R.id.request_single_username);
            typeTextView = itemView.findViewById(R.id.request_single_type);
            dateTextView = itemView.findViewById(R.id.request_single_date);
            profilePicture = itemView.findViewById(R.id.request_single_image);
        }

        public void bind(User user, String type, long timestamp){

            Glide.with(mContext)
                    .applyDefaultRequestOptions(
                            new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    ).load(user.getProfilePicURI())
                    .into(profilePicture);
            nameTextView.setText(user.getDisplayName());
            usernameTextView.setText("@" + user.getUsername());

            switch (type){
                case "sent":
                    typeTextView.setText("SENT");
                    break;
                case "received":
                    typeTextView.setText("RECEIVED");
                    break;
            }

            dateTextView.setText(Utils.convertTimestampToDate(timestamp));
        }
    }
}
