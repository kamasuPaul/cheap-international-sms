package com.softappsuganda.cheapinternationalsmsapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.softappsuganda.cheapinternationalsmsapp.helpers.Tools;

public class MessageDeliveredBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String messageId = intent.getStringExtra("id");
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Tools.updateMessagestatus(context, messageId, "Delivered");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Tools.updateMessagestatus(context, messageId, "Failed");
                break;
            default: // unknown
                Tools.updateMessagestatus(context, messageId, "Unknown");
        }
    }
}
