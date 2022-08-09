package com.softappsuganda.cheapinternationalsmsapp.helpers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class MessageWorker extends Worker
{
    // Define the parameter keys:
    public static final String MESSAGE_ID = "messageId";
    public static final String MESSAGE_STATUS = "messageStatus";
    public MessageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @Override
    public Result doWork() {

        String messageId = getInputData().getString(MESSAGE_ID);
        String messageStatus = getInputData().getString(MESSAGE_STATUS);
        FirebaseFirestore.getInstance()
                .collection("messages")
                .document(messageId)
                .update("status", messageStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                        Log.d(TAG, "SENT MESSAGE WITH ID:"+messageId);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }


}
