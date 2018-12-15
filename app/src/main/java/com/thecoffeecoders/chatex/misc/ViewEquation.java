package com.thecoffeecoders.chatex.misc;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.thecoffeecoders.chatex.R;

import io.github.kexanie.library.MathView;

public class ViewEquation extends AppCompatActivity {

    private MathView mMathView;
    private FloatingActionButton mCopyFab;

    private String mEquation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_equation);

        initializeViews();
        getSupportActionBar().setTitle("View Equation");

        mEquation = getIntent().getStringExtra("equation");
        mMathView.setText("$$" + mEquation + "$$");


        mCopyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mMathView.getText();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("equation", mEquation);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ViewEquation.this, "Equation copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        mMathView = findViewById(R.id.mathview_preview);
        mCopyFab = findViewById(R.id.fab_copy);
    }
}
