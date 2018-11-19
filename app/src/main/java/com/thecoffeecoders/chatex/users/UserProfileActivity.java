package com.thecoffeecoders.chatex.users;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.chat.ChatActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.models.User;

public class UserProfileActivity extends AppCompatActivity {

    //Firebase Objects
    FirebaseAuth mAuth;
    FirebaseUser muser;

    //Layout Views
    ImageView mUserProfileCoverPhotoImageView;
    CircularImageView mUserProfileProfilePictureImageView;
    TextView mUserProfileDisplayNameTextView;
    TextView mUserProfileUserNameTextView;
    TextView mUserProfileBioTextView;
    TextView mUserProfileAddressTextView;
    TextView mUserProfileMemberSinceTextView;
    FloatingActionButton mUserProfileFab;

    //For receiving extras in Intent
    Bundle mExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Layout files instantiation
        mUserProfileCoverPhotoImageView = findViewById(R.id.user_profile_header_cover_image);
        mUserProfileProfilePictureImageView = findViewById(R.id.user_profile_photo);
        mUserProfileDisplayNameTextView = findViewById(R.id.user_profile_name);
        mUserProfileUserNameTextView = findViewById(R.id.user_profile_username);
        mUserProfileBioTextView = findViewById(R.id.user_profile_short_bio);
        mUserProfileAddressTextView = findViewById(R.id.user_profile_address);
        mUserProfileMemberSinceTextView = findViewById(R.id.user_profile_member_since);
        mUserProfileFab = findViewById(R.id.user_profile_fab);


        mExtras = getIntent().getExtras();

        new FetchUserDetailsAndDisplayUITask().execute();

    }

    private class FetchUserDetailsAndDisplayUITask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            final String user_id = mExtras.getString("uid");

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user_id);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            User user = dataSnapshot.getValue(User.class);
                            mUserProfileFab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
                                    chatIntent.putExtra("uid", user_id);
                                    startActivity(chatIntent);
                                }
                            });
                            updateUserUI(user);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

    private void updateUserUI(User user){
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.img_profile_picture_placeholder_female);
        Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .load(user.getProfilePicURI())
                .into(mUserProfileProfilePictureImageView);
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.img_cover_photo_placeholder);
        Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .load(user.getCoverPictureURI())
                .into(mUserProfileCoverPhotoImageView);
        mUserProfileDisplayNameTextView.setText(user.getDisplayName());
        mUserProfileUserNameTextView.setText("@" + user.getUsername());
        mUserProfileBioTextView.setText(user.getBio());
        mUserProfileAddressTextView.setText(user.getAddress());
        //mUserProfileMemberSinceTextView.setText(user.getMemberSince());
    }
}
