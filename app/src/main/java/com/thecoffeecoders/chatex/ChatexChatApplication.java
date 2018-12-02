package com.thecoffeecoders.chatex;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class ChatexChatApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        //Firebase context setup
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
