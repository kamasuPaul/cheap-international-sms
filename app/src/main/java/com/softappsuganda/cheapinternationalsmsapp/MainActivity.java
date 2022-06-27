package com.softappsuganda.cheapinternationalsmsapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    Button sendMessageButton;
    EditText phoneNumber, messageText;
    FirebaseFirestore db;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = FirebaseFirestore.getInstance();
        preferences = getSharedPreferences("messages", Context.MODE_PRIVATE);
        db.collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
//                Toast.makeText(MainActivity.this, "NEW MESSAGE DOC", Toast.LENGTH_SHORT).show();

                for (QueryDocumentSnapshot doc : value) {
                    String smsText = doc.getString("sms_text");
                    String phoneNumber = doc.getString("phone");
                    String status = doc.getString("status");
                    if (status.equalsIgnoreCase("pending")) {
                        String documentId = doc.getId();
                        if (preferences.getString(documentId, null) == null) {
                            sendSms(phoneNumber, smsText);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(documentId, smsText);
                            editor.commit();
                        }
                        db.collection("messages").document(documentId).update("status", "sent");
                    }
                }
            }
        });

        sendMessageButton = findViewById(R.id.buttonSendMessage);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        messageText = findViewById(R.id.editTextMessage);
        //send sms using sms manager
        sendMessageButton.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                String number = phoneNumber.getText().toString();
                String smsText = messageText.getText().toString();
                Map<String,String> data = new HashMap<>();
                data.put("phone",number);
                data.put("sms_text",smsText);
                data.put("status","Pending");

                db.collection("messages").document().set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Message queued", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    public int getSubscriptionNumber(String phoneNumber, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(context, "No permission", Toast.LENGTH_SHORT).show();
                return -1;
            }
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
                    Log.d("MAIN", this.getNetwork(phoneNumber));
                    Log.d("MAIN", String.valueOf(subscriptionId));

                    if (networkName.contains(this.getNetwork(phoneNumber))) {
                        return subscriptionId;
                    }
                }
            }
        }
        return -1;
    }

    public String getNetwork(String number) {
        if (number.startsWith("075")) {
            return "AIRTEL";
        } else if (number.startsWith("078")) {
            return "LYCA";
        } else {
            return "MTN";
        }
    }

    private void sendSms(String number, String smsText) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        SmsManager smsManager = null;
        int subscriptionId = getSubscriptionNumber(number, MainActivity.this);
        if (subscriptionId == -1) {
            smsManager = SmsManager.getDefault();
//            Toast.makeText(MainActivity.this, "subscription id ---1", Toast.LENGTH_SHORT).show();
        } else {
            smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
//            Toast.makeText(MainActivity.this, "sub:" + subscriptionId, Toast.LENGTH_SHORT).show();
        }
        smsManager.sendTextMessage(number, null, smsText, pendingIntent, null);
        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
    }
}