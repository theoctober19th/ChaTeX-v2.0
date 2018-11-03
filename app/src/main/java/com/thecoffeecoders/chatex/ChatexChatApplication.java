package com.thecoffeecoders.chatex;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class ChatexChatApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        //Firebase context setup
        FirebaseApp.initializeApp(this);
    }
}
