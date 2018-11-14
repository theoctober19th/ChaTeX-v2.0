package com.thecoffeecoders.chatex.users;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.thecoffeecoders.chatex.MainActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.auth.LoginActivity;
import com.thecoffeecoders.chatex.models.User;

import java.util.Arrays;
import java.util.Map;

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

    User user;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Firebase Objects
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        user = new User();

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
                new PopulateFormWithUserDataFromProvider().execute();
                emailEditText.setEnabled(false);
            } else{

            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class PopulateFormWithPreExistingUserData extends  AsyncTask<Void, Void, User>{

        @Override
        protected User doInBackground(Void... voids) {


            return null;
        }
    }

    private class PopulateFormWithUserDataFromProvider extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            user.setId(mUser.getUid());

            if(bundleExtras != null){
                if(bundleExtras.getBoolean("isNew")){
                    if(bundleExtras.getString("provider").equals("firebase")){
                        user.setDisplayName(mUser.getDisplayName());
                        user.setProfilePicURI(mUser.getPhotoUrl().toString());
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
                            user.setProfilePicURI(photoUrl.toString());
                        }
                    }
                }else {//preexisting user
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+mUser.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                user = dataSnapshot.getValue(User.class);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("firebasedatabase", "loadPost:onCancelled", databaseError.toException());
                        }
                    });
                }
            }

            //return user;
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

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

            if(user.getCoverPictureURI() != null){
                //Load Profile Picture
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_cover_photo_placeholder);
                Glide.with(EditProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(user.getCoverPictureURI())
                        .into(coverPictureImgView);
            }

            if(user.getDisplayName() != null){
                nameEditText.setText(user.getDisplayName());
            }

            if(user.getUsername() != null){
                usernameEditText.setText(user.getUsername());
            }

            if(user.getBio() != null){
                bioEditText.setText(user.getBio());
            }

            if(user.getGender() != null){
                genderSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.gender_array)).indexOf(user.getGender()));
            }

            if(user.getEmail() != null){
                emailEditText.setText(user.getEmail());
            }

            if(user.getAddress() != null){
                addressEditText.setText(user.getAddress());
            }

            updateInfoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserInformation();
                }
            });
        }
    }

    private void updateUserInformation() {
        startProgressDialog("Updating information", "Please wait");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid());

        User updatedUser = new User();
        updatedUser.setId(mUser.getUid());
        updatedUser.setCoverPictureURI(user.getCoverPictureURI());
        updatedUser.setProfilePicURI(user.getProfilePicURI());
        updatedUser.setDisplayName(nameEditText.getText().toString());
        updatedUser.setUsername(usernameEditText.getText().toString());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setBio(bioEditText.getText().toString());
        updatedUser.setGender(genderSpinner.getSelectedItem().toString());
        updatedUser.setAddress(addressEditText.getText().toString());

        userRef.setValue(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressDialog();

                //Take to MainActivity
                Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(mainIntent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                createAlertDialog("Error Writing to Database", "Please check your internet connection");
            }
        });
    }


    public void promptCoverPictureChange(View view){

    }

    public void promptProfilePictureChange(View view){

    }

    public void startProgressDialog(String title, String message){
        mProgressDialog = new ProgressDialog(EditProfileActivity.this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    public void hideProgressDialog(){
        mProgressDialog.dismiss();
    }

    private void createAlertDialog(String title, String alertMessage){
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfileActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(alertMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
