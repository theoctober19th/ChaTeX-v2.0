package com.thecoffeecoders.chatex.auth;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thecoffeecoders.chatex.MainActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.users.EditProfileActivity;

import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {

    //Facebook SDK objects
    private CallbackManager mCallbackManager;

    //Google Sign In API objects
    private GoogleSignInClient mGoogleSignInClient;

    //Firebase Objects
    private FirebaseAuth mAuth;

    private final String TAG = "FACEBOOK";
    private static final int RC_SIGN_IN_GOOGLE = 123;
    private String provider = "firebase";

    //Layout Elements
    private FloatingActionButton mFacebookLoginBtn;
    private FloatingActionButton mGoogleLoginBtn;
    private LinearLayout mRegisterLinearLayout;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mRetypePasswordEditText;
    private Button mRegisterButton;

    //Logging in Progress Dialog
    private ProgressDialog mFacebookLoginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        instantiateViews();

        //Initialize general register button
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserWithEmail();
            }
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginBtn  = findViewById(R.id.register_buttonFacebookLogin);
        mFacebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(RegisterActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);

                        //when facebook login is successful
                        startLoadingAnimation();
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        // ...
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        createAlertDialog("Authentication failed", "No internet connection");
                        // ...
                    }
                });
            }
        });

        // Configure Google Sign In
        mGoogleLoginBtn = findViewById(R.id.register_buttonGoogleLogin);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient  = GoogleSignIn.getClient(this, gso);
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

    }

    public void signInWithGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            provider = "facebook";
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {

        if(currentUser != null){

            //Check if user already exists (means it is not the first time the user is logging in)
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){ //user already exists

                        //Take user to MainActivity
                        if(mFacebookLoginDialog != null) stopLoadingAnimation();
                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mainIntent.putExtra("provider", provider);
                        finish();
                        startActivity(mainIntent);
                    }else{

                        //Take user to EditProfileActivity
                        if(mFacebookLoginDialog != null) stopLoadingAnimation();
                        Intent editProfileIntent = new Intent(RegisterActivity.this, EditProfileActivity.class);
                        editProfileIntent.putExtra("isNew", true);
                        editProfileIntent.putExtra("provider", provider);
                        startActivity(editProfileIntent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }

    private void registerUserWithEmail() {
        final String email = mEmailEditText.getText().toString();
        final String password = mPasswordEditText.getText().toString();
        final String retypedPassword = mRetypePasswordEditText.getText().toString();

        if(TextUtils.isEmpty(email)){
            mEmailEditText.setError("This field cannot be blank");
            mEmailEditText.requestFocus();
            return;
        }else if(TextUtils.isEmpty(password)){
            mPasswordEditText.setError("Password cannot be blank");
            mPasswordEditText.requestFocus();
            return;
        }else if(!password.equals(retypedPassword)){
            mRetypePasswordEditText.setError("Retyped password does not match");
            mRetypePasswordEditText.requestFocus();
            return;
        }


        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            firebaseCreateUserWithEmailAndPassword(email, password);
        }else{
            mEmailEditText.setError("Invalid email address");
            mEmailEditText.requestFocus();
        }
    }

    private void instantiateViews() {
        mRegisterLinearLayout = findViewById(R.id.register_linear_layout);
        mEmailEditText = findViewById(R.id.register_email_et);
        mPasswordEditText = findViewById(R.id.register_password_et);
        mRegisterButton = findViewById(R.id.register_button);
        mRetypePasswordEditText = findViewById(R.id.register_retype_password_et);
    }

    public void inflateLoginLayout(View view) {
        Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);

        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(mRegisterLinearLayout, "layoutTransition");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(RegisterActivity.this, pairs);
        startActivity(registerIntent, options.toBundle());
        finish();
    }

    public void startLoadingAnimation(){
        mFacebookLoginDialog = new ProgressDialog(RegisterActivity.this);
        mFacebookLoginDialog.setTitle("Logging in");
        mFacebookLoginDialog.setMessage("Please Wait..");
        mFacebookLoginDialog.setCanceledOnTouchOutside(false);
        mFacebookLoginDialog.show();
    }

    public void stopLoadingAnimation(){
        mFacebookLoginDialog.dismiss();
    }

    private void createAlertDialog(String title, String alertMessage){
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
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

    public void firebaseCreateUserWithEmailAndPassword(String email, String password){

        startLoadingAnimation();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            SharedPreferences.Editor editor = getSharedPreferences("com.thecoffeecoders.chatex", MODE_PRIVATE).edit();
                            editor.putBoolean("email_verified", user.isEmailVerified());
                            editor.apply();
                            updateUI(user);

                            updateUI(user);
                        } else {
                            stopLoadingAnimation();
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                createAlertDialog("Registration Error", "The entered password is too weak. Please choose a strong password.");
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                createAlertDialog("Registration Error", "The credentials provided are invalid. Please provide valid credentials");
                            } catch(FirebaseAuthUserCollisionException e) {
                                createAlertDialog("Registration Error", "An user with the entered email already exists. Please login with your password.");
                            } catch(Exception e) {
                                createAlertDialog("Registration Error", "Please check the credentials and your internet connection");
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        if(requestCode != RC_SIGN_IN_GOOGLE) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                startLoadingAnimation();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            provider = "google";
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            stopLoadingAnimation();
                            //Snackbar.make(findViewById(R.id.loginParentConstraintLayout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            createAlertDialog("Authentication failed", "Check your internet connection and app permissions.");
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

}
