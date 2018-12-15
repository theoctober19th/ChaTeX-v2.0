package com.thecoffeecoders.chatex.math;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.adapters.EquationAdapter;
import com.thecoffeecoders.chatex.adapters.SuggestionRecyclerAdapter;
import com.thecoffeecoders.chatex.db.EquationContract;
import com.thecoffeecoders.chatex.db.EquationDBHelper;
import com.thecoffeecoders.chatex.interfaces.OnAdapterItemClicked;
import com.thecoffeecoders.chatex.models.Equation;
import com.thecoffeecoders.chatex.utils.Utils;

import io.github.kexanie.library.MathView;

public class WriteEquationActivity extends AppCompatActivity implements OnAdapterItemClicked {

    SQLiteDatabase mLocalDatabase;

    EditText mTypedEquationEditText;
    MathView mPreviewMathView;
    FloatingActionButton mSendEquationFab;
    RecyclerView mSuggestionRecyclerView;
    SuggestionRecyclerAdapter mSuggestionRecyclerAdapter;

    DatabaseReference mSuggestionRef;
    Query mSuggestionQuery;

    EquationAdapter mEquationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_equation);

        Log.d("token", FirebaseInstanceId.getInstance().getToken());

        getSupportActionBar().setTitle("Write Equation");

        EquationDBHelper dbHelper = new EquationDBHelper(this);
        mLocalDatabase = dbHelper.getWritableDatabase();
        initializeViews();
        setAppropriateListeners();
    }

    private void initializeViews(){
        mTypedEquationEditText = findViewById(R.id.et_typed_equation);
        mPreviewMathView = findViewById(R.id.math_preview_pane);
        mSendEquationFab = findViewById(R.id.fab_send_equation);
        mSuggestionRecyclerView = findViewById(R.id.suggestion_recyclerview);
        mSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void addSuggestionAdapter(){
//        mSuggestionQuery = FirebaseDatabase
//                .getInstance()
//                .getReference()
//                .child("equations")
//                .child("suggestions")
//                .orderByChild("equation")
//                .startAt(mTypedEquationEditText.getText().toString())
//                .endAt("\uf8ff");
//        FirebaseRecyclerOptions<Equation> options = new FirebaseRecyclerOptions.Builder<Equation>()
//                .setQuery(mSuggestionQuery, Equation.class)
//                .build();
//        mSuggestionRecyclerAdapter = new SuggestionRecyclerAdapter(options);
//        mSuggestionRecyclerView.setAdapter(mSuggestionRecyclerAdapter);

        mEquationAdapter = new EquationAdapter(this, getEquationSuggestions(), this);
        mSuggestionRecyclerView.setAdapter(mEquationAdapter);

    }

    protected void onLatexButtonClicked(View view){
        String latexCode = view.getTag().toString();

        //mTypedEquationEditText.setText(latexCode);
        mTypedEquationEditText.getText().insert(mTypedEquationEditText.getSelectionStart(), latexCode);
    }

    protected void onSuggestionClicked(View view){
        if(mSuggestionRecyclerView.getVisibility() == View.VISIBLE){
            //suggestions is visible
            //mSuggestionRecyclerAdapter.stopListening();
            mSuggestionRecyclerView.setVisibility(View.GONE);

        }else{
            //suggestion is not visible
            mSuggestionRecyclerView.setVisibility(View.VISIBLE);
            Utils.hideKeyboard(this);
            //mSuggestionRecyclerAdapter.startListening();
        }
    }

    public void setAppropriateListeners(){

        mTypedEquationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPreviewMathView.setText("$$" + s.toString() + "$$");
                addSuggestionAdapter();
                //Utils.hideKeyboard(WriteEquationActivity.this);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSendEquationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //adding to local database
                ContentValues contentValues = new ContentValues();
                contentValues.put(EquationContract.EquationEntry.COLUMN_EQUATION, mTypedEquationEditText.getText().toString());
                mLocalDatabase.insert(EquationContract.EquationEntry.TABLE_NAME, null, contentValues);

                //Add equation to the history
                final DatabaseReference eqnhistoryRef =
                        FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("equations")
                        .child("history")
                        .child(FirebaseAuth.getInstance().getUid())
                        .push();

                final DatabaseReference eqnSuggestionRef
                        =FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("equations")
                        .child("suggestions")
                        .push();

                Equation equation = new Equation();
                equation.setEquation(mTypedEquationEditText.getText().toString());

                eqnSuggestionRef.setValue(equation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

//                eqnSuggestionRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists()){
//                            int usage = dataSnapshot.getValue(Integer.class);
//                            usage ++;
//                            eqnSuggestionRef.setValue(usage);
//                        }else{
//                            int usage = 1;
//                            eqnSuggestionRef.setValue(usage);
//                        }
//
//                        eqnhistoryRef.setValue(mTypedEquationEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//                });

                //Set the result and send the user back to ChatActivity
                Intent returnIntent = new Intent();
                returnIntent.putExtra("equation", mTypedEquationEditText.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private Cursor getEquationSuggestions(){
        return mLocalDatabase.rawQuery(
                    "SELECT " + EquationContract.EquationEntry.COLUMN_EQUATION +
                        " FROM " + EquationContract.EquationEntry.TABLE_NAME +
                        " WHERE " + EquationContract.EquationEntry.COLUMN_EQUATION + " LIKE \"" +
                        mTypedEquationEditText.getText().toString() + "%\"" +
                        " LIMIT 5",
                null
        );
    }

    @Override
    public void onAdapterItemClicked(String value) {
        mTypedEquationEditText.getText().clear();
        mTypedEquationEditText.setText(value);
    }
}
