package com.softappsuganda.cheapinternationalsmsapp.helpers;


import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softappsuganda.cheapinternationalsmsapp.MessageDeliveredBroadcastReceiver;
import com.softappsuganda.cheapinternationalsmsapp.MessageSentBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tools {
    public static String TAG = "MAINACTIVE";
    public static String ACTION_SMS_SENT = "com.softappsuganda.cheapinternationalsmsapp.action.ACTION_SMS_SENT";
    public static String ACTION_SMS_DELIVERED = "com.softappsuganda.cheapinternationalsmsapp.action.ACTION_SMS_DELIVERED";
    public  static int getSubscriptionNumber(String phoneNumber, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (!checkPermission(context)) return -1;
            List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
            if (subList != null) {

                for (final SubscriptionInfo subscriptionInfo : subList) {
                    final String networkName = subscriptionInfo.getCarrierName().toString().toUpperCase();
                    String mcc = String.valueOf(subscriptionInfo.getMcc());
                    String mnc = String.valueOf(subscriptionInfo.getMnc());
                    String hnc = mcc + mnc;
                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                    Log.d("MAIN", networkName);
                    Log.d("MAIN", Tools.getNetwork(phoneNumber));
                    Log.d("MAIN", String.valueOf(subscriptionId));

                    if (networkName.contains(Tools.getNetwork(phoneNumber))) {
                        return subscriptionId;
                    }
                }
            }
        }
        return -1;
    }

    public static boolean checkPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "No permission");
            return false;
        }
        return true;
    }

    public static String getNetwork(String number) {
        //TODO IMPLEMENT DETECTING NETWORK FROM HNC STORED IN FIREBASE
//        if(true){
//            return "LYCA";
//        }
        if (number.startsWith("075")
                ||number.startsWith("+25675")
                ||number.startsWith("+25670")
                ||number.startsWith("+25674")
                ||number.startsWith("+25620")
        ) {
            return "AIRTEL";
        } else if (number.startsWith("078")
                ||number.startsWith("077")
                ||number.startsWith("+25678")
                ||number.startsWith("+25677")
                ||number.startsWith("+25676")
                ||number.startsWith("+25639")
                ||number.startsWith("+25631")
        ) {
            return "MTN";
        } else {
            return "LYCA";
        }
    }

    public static void sendSms(Context context, String number, String smsText, String messageID) {
        //drop the sms if device is disconnected
        Boolean allowSend = context.getSharedPreferences("messages", Context.MODE_PRIVATE)
                .getBoolean("allow_send", false);
        if(!allowSend){
            Log.d(TAG,"Sending not allowed by device");
            return;
        }
        Intent intent = new Intent(ACTION_SMS_SENT);
        intent.setClass(context, MessageSentBroadcastReceiver.class);
        intent.putExtra("id",messageID);

        Intent intentDelivered = new Intent(ACTION_SMS_DELIVERED);
        intentDelivered.setClass(context,MessageDeliveredBroadcastReceiver.class);
        intentDelivered.putExtra("id",messageID);


        PendingIntent sentIntent = PendingIntent.getBroadcast(context,0 , intent, FLAG_UPDATE_CURRENT );
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(context,0 , intentDelivered, FLAG_UPDATE_CURRENT );

        SmsManager smsManager = null;
        int subscriptionId = getSubscriptionNumber(number, context);
        if (subscriptionId != -1)
            smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        else smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, smsText, sentIntent, deliveredIntent);
    }
    public static ArrayList<String> getTopics(Context context) {
        ArrayList <String> topics = new ArrayList<>();
        if(!checkPermission(context)) return null;
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subList != null) {

            for (final SubscriptionInfo subscriptionInfo : subList) {
                final String networkName = subscriptionInfo.getCarrierName().toString().toUpperCase();
                String mcc = String.valueOf(subscriptionInfo.getMcc());
                String mnc = String.valueOf(subscriptionInfo.getMnc());
                String hnc = mcc + mnc;
                topics.add(hnc);
            }
        }
        return topics;
    }
    public static HashMap<String,String> getNetworks(Context context) {
        HashMap<String,String> networks = new HashMap<String,String>();
        if(!checkPermission(context)) return null;
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subList != null) {

            for (final SubscriptionInfo subscriptionInfo : subList) {
                final String networkName = subscriptionInfo.getCarrierName().toString().toUpperCase();
                String mcc = String.format("%02d",(subscriptionInfo.getMcc()));
                String mnc = String.format("%02d",(subscriptionInfo.getMnc()));
                String hnc = mcc + mnc;
                networks.put(hnc,networkName);
            }
        }
        return networks;
    }
    public static void updateMessagestatus(Context context,String messageId, String status){
        //create a background job to update message status when device is connected
        //pass arguments to the work object
        Data myData = new Data.Builder()
                .putString(MessageWorker.MESSAGE_ID, messageId)
                .putString(MessageWorker.MESSAGE_STATUS, status)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest updateWorkRequest =
                new OneTimeWorkRequest.Builder(MessageWorker.class)
                        .setConstraints(constraints)
                        .setInputData(myData)
                        .build();
        WorkManager
                .getInstance(context)
                .enqueue(updateWorkRequest);

    }
}
