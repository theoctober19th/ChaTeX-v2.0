package com.thecoffeecoders.chatex.math;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.thecoffeecoders.chatex.R;

import io.github.kexanie.library.MathView;

public class WriteEquationActivity extends AppCompatActivity {


    EditText mTypedEquationEditText;
    MathView mPreviewMathView;
    FloatingActionButton mSendEquationFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_equation);

        initializeViews();
        setAppropriateListeners();
    }

    private void initializeViews(){
        mTypedEquationEditText = findViewById(R.id.et_typed_equation);
        mPreviewMathView = findViewById(R.id.math_preview_pane);
        mSendEquationFab = findViewById(R.id.fab_send_equation);
    }

    protected void onLatexButtonClicked(View view){
        String latexCode = view.getTag().toString();

        //mTypedEquationEditText.setText(latexCode);
        mTypedEquationEditText.getText().insert(mTypedEquationEditText.getSelectionStart(), latexCode);
    }

    public void setAppropriateListeners(){
        mTypedEquationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPreviewMathView.setText("$$" + s.toString() + "$$");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSendEquationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("equation", mTypedEquationEditText.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
