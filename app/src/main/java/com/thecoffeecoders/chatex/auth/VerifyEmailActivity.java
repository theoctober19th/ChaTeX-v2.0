package com.thecoffeecoders.chatex.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thecoffeecoders.chatex.MainActivity;
import com.thecoffeecoders.chatex.R;

public class VerifyEmailActivity extends AppCompatActivity {

    Button mSendVerificationButton;
    Button mLoginButton;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        getSupportActionBar().setTitle("Verify Your Email");

        initializeViews();
        initializeFirebase();

        mSendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            createAlertDialog("Verification Link Sent", "Please check your email inbox for the email containing the verification link.");
                        }
                    }
                });
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            @Override
            public void onClick(View v) {
                if(currentUser != null){
                    if(currentUser.isEmailVerified()){

                        SharedPreferences.Editor editor = getSharedPreferences("com.thecoffeecoders.chatex", MODE_PRIVATE).edit();
                        editor.putBoolean("email_verified", mUser.isEmailVerified());
                        editor.apply();

//                        Intent mainIntent = new Intent(VerifyEmailActivity.this, MainActivity.class);
//                        startActivity(mainIntent);
//                        finish();

                    }else{
                        //Toast.makeText(VerifyEmailActivity.this, "Email not yet verified.", Toast.LENGTH_SHORT).show();
                    }
                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();
                    Intent loginIntent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    private void initializeViews(){
        mSendVerificationButton  = findViewById(R.id.verify_email_send_link_button);
        mLoginButton = findViewById(R.id.verify_email_login_button);
    }

    private void createAlertDialog(String title, String alertMessage){
        AlertDialog alertDialog = new AlertDialog.Builder(VerifyEmailActivity.this).create();
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
