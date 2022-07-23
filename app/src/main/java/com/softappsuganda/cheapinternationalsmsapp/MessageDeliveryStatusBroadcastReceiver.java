package com.softappsuganda.cheapinternationalsmsapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.softappsuganda.cheapinternationalsmsapp.helpers.Tools;

public class MessageDeliveryStatusBroadcastReceiver extends BroadcastReceiver {
    public static String TAG = "MAINACTIVE";
    @Override
    public void onReceive(Context context, Intent intent) {
        String messageId = intent.getStringExtra("id");
        switch(getResultCode()){
            case Activity.RESULT_OK:
                Tools.updateMessagestatus(context, messageId,"Sent");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Tools.updateMessagestatus(context, messageId,"Failed");
                break;
            default: // unknown
                Tools.updateMessagestatus(context, messageId,"Unknown");
        }
    }
}
