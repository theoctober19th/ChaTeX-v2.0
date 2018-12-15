package com.thecoffeecoders.chatex.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.thecoffeecoders.chatex.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText mEmailEditText;
    Button mResetPasswordButton;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        initializeFirebase();

        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Email button is clicked..
                final String email = mEmailEditText.getText().toString();
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEditText.setError("Email not valid.");
                    return;
                }

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        createAlertDialog("Verification Email Sent", "Please check inbox of email address " + email + " for reset link.");
                    }
                });

            }
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        mEmailEditText = findViewById(R.id.forgot_password_edittext);
        mResetPasswordButton = findViewById(R.id.forgot_password_button);
    }

    private void createAlertDialog(String title, String alertMessage){
        AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this).create();
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
