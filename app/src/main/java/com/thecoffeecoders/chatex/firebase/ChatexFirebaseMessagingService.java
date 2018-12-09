package com.thecoffeecoders.chatex.firebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thecoffeecoders.chatex.ChatexChatApplication;
import com.thecoffeecoders.chatex.MainActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.utils.Utils;

import java.io.Console;

public class ChatexFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        if(FirebaseAuth.getInstance().getUid() == null) {
            SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(ChatexChatApplication.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
            sharedPrefEditor.putString("device_token", s);
            sharedPrefEditor.putBoolean("device_token_updated", false);
            sharedPrefEditor.apply();
        }else{
            SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(ChatexChatApplication.SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
            sharedPrefEditor.putString("device_token", s);
            sharedPrefEditor.putBoolean("device_token_updated", true);
            sharedPrefEditor.apply();

            final DatabaseReference tokenIDRef =
                    FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("deviceToken");
            tokenIDRef.setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.e("firebase_log", "Device token updated for user " + FirebaseAuth.getInstance().getUid());
                    }
                }
            });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String content = remoteMessage.getNotification().getBody();
        String title = remoteMessage.getNotification().getTitle();
        String senderUID = remoteMessage.getNotification().getTag();

        Log.d("notificationtag", senderUID);

//        if(senderUID.equals("") || senderUID.equals(null)){
//            senderUID = "general_notification";
//        }

//        Log.d("nofificationbody", content);

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.chatex_app_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(senderUID, Utils.getUniqueInteger(), mBuilder.build());
    }
}
