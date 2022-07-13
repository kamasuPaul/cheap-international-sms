package com.softappsuganda.cheapinternationalsmsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.softappsuganda.cheapinternationalsmsapp.helpers.Tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAINACTIVE";// MainActivity.class.getSimpleName();
    Button sendMessageButton;
    EditText phoneNumber, messageText;
    FirebaseFirestore db;
    // Write a message to the database
    FirebaseDatabase database;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        preferences = getSharedPreferences("messages", Context.MODE_PRIVATE);
//        db.collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.w(TAG, "Listen failed.", error);
//                    return;
//                }
////                Toast.makeText(MainActivity.this, "NEW MESSAGE DOC", Toast.LENGTH_SHORT).show();
//
//                for (QueryDocumentSnapshot doc : value) {
//                    String smsText = doc.getString("sms_text");
//                    String phoneNumber = doc.getString("phone");
//                    String status = doc.getString("status");
//                    if (status.equalsIgnoreCase("pending")) {
//                        String documentId = doc.getId();
//                        if (preferences.getString(documentId, null) == null) {
//                            Tools.sendSms(MainActivity.this, phoneNumber, smsText);
//                            SharedPreferences.Editor editor = preferences.edit();
//                            editor.putString(documentId, smsText);
//                            editor.commit();
//                        }
//                        db.collection("messages").document(documentId).update("status", "sent");
//                    }
//                }
//            }
//        });

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
                Map<String, String> data = new HashMap<>();
                data.put("phone", number);
                data.put("sms_text", smsText);
                data.put("status", "Pending");

                db.collection("messages").document().set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Message queued", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("device_token", token);
                        editor.commit();

                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, token);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                        setPresence();
                    }
                });

        //subscribe to a network topic
//        this.subscribeToTopics();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(MainActivity.this, "my code", Toast.LENGTH_LONG).show();
        Log.d(TAG, String.valueOf(resultCode));
    }

    public void subscribeToTopics() {
        List<String> topics = Tools.getTopics(getApplicationContext());
        for (String topic : topics) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Subscribed";
                            if (!task.isSuccessful()) {
                                msg = "Subscribe failed";
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /* This is where we will store data about being online/offline. */
    public void setPresence() {
        // Create a reference to this user's specific status node.
        String device_token = preferences.getString("device_token", null);
        // Since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference networksRef = database.getReference("users/" + device_token + "/networks");
        final DatabaseReference presenceRef = database.getReference("users/" + device_token + "/status");


        // Stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/users/" + device_token + "/last_active_at");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
//                     = myConnectionsRef.push();
                    presenceRef.setValue("online");
                    //set the networks
                    HashMap<String, String> networks = Tools.getNetworks(getApplicationContext());
                    int index = 0;
                    for (Map.Entry<String, String> network : networks.entrySet()) {
                        String hnc = network.getKey();
                        Object networkName = network.getValue();
                        DatabaseReference networkRef = networksRef.child(String.valueOf(index));
                        networkRef.child("name").setValue(networkName);
                        networkRef.child("hnc").setValue(hnc);
                        index++;
                    }

                    // When this device disconnects, remove it
//                    networksRef.onDisconnect().setValue("");
                    presenceRef.onDisconnect().setValue("offline");

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    //set the token
                    database.getReference("users/" + device_token + "/token")
                            .setValue(device_token);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Listener was cancelled at .info/connected");
            }
        });
    }
}