package com.thecoffeecoders.chatex;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.thecoffeecoders.chatex.firebase.ChatexFirebaseMessagingService;

public class ChatexChatApplication extends Application{

    //GLOBAL CONSTANTS SECTION
    public static final String SHARED_PREFERENCE_NAME = "com.thecoffeecoders.chatex";


    @Override
    public void onCreate() {
        super.onCreate();

        //Firebase context setup
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_1";
            String description = "default_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ChatexFirebaseMessagingService.CHANNEL_ID_1, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
