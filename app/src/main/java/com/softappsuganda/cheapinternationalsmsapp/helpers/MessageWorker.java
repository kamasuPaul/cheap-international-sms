package com.softappsuganda.cheapinternationalsmsapp.helpers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.concurrent.futures.CallbackToFutureAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;

public class MessageWorker extends ListenableWorker
{
    // Define the parameter keys:
    public static final String MESSAGE_ID = "messageId";
    public static final String MESSAGE_STATUS = "messageStatus";
    public MessageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            String messageId = getInputData().getString(MESSAGE_ID);
            String messageStatus = getInputData().getString(MESSAGE_STATUS);
            FirebaseFirestore.getInstance()
                    .collection("messages")
                    .document(messageId)
                    .update("status", messageStatus)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("SUCCESS",messageStatus+messageId);
                            completer.set(Result.success());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FAILURE",messageStatus+messageId);
                            completer.set(Result.retry());
                        }
                    });

            // This value is used only for debug purposes: it will be used
            // in toString() of returned future or error cases.
            return "startWorkMethod";
        });

    }


}
