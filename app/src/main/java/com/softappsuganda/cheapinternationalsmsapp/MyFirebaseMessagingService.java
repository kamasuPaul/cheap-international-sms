package com.softappsuganda.cheapinternationalsmsapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;
//import androidx.work.WorkRequest;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.softappsuganda.cheapinternationalsmsapp.helpers.MessageWorker;
import com.softappsuganda.cheapinternationalsmsapp.helpers.Tools;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MAINACTIVE";

    public MyFirebaseMessagingService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob(getApplicationContext(), remoteMessage);
            } else {
                // Handle message within 10 seconds
                handleNow(getApplicationContext(),remoteMessage);
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
//            Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNow(getApplicationContext(),remoteMessage);

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    private void scheduleJob(Context context, RemoteMessage remoteMessage){

//        WorkRequest sendMessageWorkRequest =
//                new OneTimeWorkRequest.Builder(MessageWorker.class)
//                        .build();
//
//        WorkManager
//                .getInstance(context)
//                .enqueue(sendMessageWorkRequest);

    }
    private void handleNow(Context context, RemoteMessage remoteMessage){
        Map<String, String> data = remoteMessage.getData();
        String message = data.get("message");
        String phone_number = data.get("phone");
        String messageId = data.get("message_id");

        if(phone_number !=null && message !=null){
            Tools.sendSms(context,phone_number,message,messageId);
        }else{
            Tools.sendSms(context,"0750883001", remoteMessage.getNotification().getBody(),messageId);
        }
    }
}