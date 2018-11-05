package com.thecoffeecoders.chatex.users;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.models.User;

public class EditProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Bundle bundleExtras;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    //UI elements
    ImageView coverPictureImgView;
    CircularImageView profilePictureImgView;
    CircularImageView changeProfilePictureImgView;
    CircularImageView changeCoverPictureImgView;
    EditText nameEditText;
    EditText usernameEditText;
    EditText bioEditText;
    Spinner genderSpinner;
    EditText emailEditText;
    EditText addressEditText;
    Button updateInfoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Firebase Objects
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        //Instantiate layout elements
        coverPictureImgView = findViewById(R.id.imgCoverPicture);
        profilePictureImgView = findViewById(R.id.imgProfilePicture);
        changeProfilePictureImgView = findViewById(R.id.imgChangeProfilePicture);
        changeCoverPictureImgView = findViewById(R.id.imgChangeCoverPicture);
        nameEditText = findViewById(R.id.et_name);
        usernameEditText = findViewById(R.id.et_username);
        bioEditText = findViewById(R.id.et_bio);

        emailEditText = findViewById(R.id.et_email);
        addressEditText = findViewById(R.id.et_address);
        updateInfoBtn = findViewById(R.id.btnUpdateInfo);

        //setting up gender spinner
        genderSpinner = findViewById(R.id.spinnerGender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);

        bundleExtras = getIntent().getExtras();
        if(bundleExtras != null){
            if(bundleExtras.getBoolean("isNew")){
                new PopulateFormWithUserData().execute();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class PopulateFormWithUserData extends AsyncTask<Void, Void, User>{

        @Override
        protected User doInBackground(Void... voids) {
            User user = new User();
            user.setId(mUser.getUid());

            if(bundleExtras != null){
                if(bundleExtras.getString("provider").equals("firebase")){
                    user.setDisplayName(mUser.getDisplayName());
                    user.setProfilePicURI(mUser.getPhotoUrl());
                    user.setEmail(mUser.getEmail());
                }else if(bundleExtras.getString("provider").equals("google")|| bundleExtras.getString("provider").equals("facebook")){
                    for (UserInfo profile : mUser.getProviderData()) {
                        // Name, email address, and profile photo Url
                        String name = profile.getDisplayName();
                        user.setDisplayName(name);
                        String email = profile.getEmail();
                        user.setEmail(email);
                        Uri photoUrl;
                        if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                            String facebookUserId = profile.getUid();
                            photoUrl = Uri.parse("https://graph.facebook.com/" + facebookUserId + "/picture?height=500");
                            Log.d("bikalpa", "https://graph.facebook.com/" + facebookUserId + "/picture?height=500");
                        }else{
                            photoUrl = profile.getPhotoUrl();
                            photoUrl = Uri.parse(photoUrl.toString().replace("/s96-c/","/s500-c/"));
                        }
                        user.setProfilePicURI(photoUrl);
                    }
                }
            }

            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            Log.d("bikalpa", "user data downloaded");

            if(user.getProfilePicURI() != null){
                //Load Profile Picture
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_profile_picture_placeholder_female);
                Glide.with(EditProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(user.getProfilePicURI())
                        .into(profilePictureImgView);
            }

            if(user.getDisplayName() != null){
                nameEditText.setText(user.getDisplayName());
            }

            if(user.getEmail() != null){
                emailEditText.setText(user.getEmail());
            }
        }
    }


    public void promptCoverPictureChange(View view){

    }

    public void promptProfilePictureChange(View view){

    }
}
