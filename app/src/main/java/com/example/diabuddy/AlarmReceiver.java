package com.example.diabuddy;

import static com.example.diabuddy.messages.MessagesViewModel.GROUP_SIZE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private CollectionReference messagesDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("messages");
    @Override
    public void onReceive(Context context, Intent intent) {
        String uid = intent.getStringExtra("uid");

        int type = 4;
        String msg = context.getResources().getString(R.string.actual_reminder_text);
        messagesDB.document("messages").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ArrayList<Pair<Integer, String>> list = new ArrayList<>();
                if (task.isSuccessful()) {
                    try {
                        List<DocumentReference> groups = (List<DocumentReference>) task.getResult().get("groups");
                        int index = groups.size()-1;
                        DocumentReference lastGroup = groups.get(index);
                        lastGroup.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                if (task1.isSuccessful()) {
                                    List<Integer> typeList = (List<Integer>) task1.getResult().get("type");
                                    if (typeList.size() == GROUP_SIZE) {
                                        DocumentReference ref = addMessageToGroup(type, msg, index+1);
                                        messagesDB.document("messages").update("groups", FieldValue.arrayUnion(ref));
                                    } else {
                                        addMessageToGroup(type, msg, index);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("MessagesViewModel", e.getMessage());
                    }
                }
            }
        });

        int notifID = 101;
        String channelID = "com.example.diabuddy.reminders";
        Intent i = new Intent(context, LoginActivity.class); // redirect to MessagesActivity after login
        i.putExtra("type", "reminder");
        PendingIntent pending = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context, channelID)
                .setContentTitle(context.getResources().getString(R.string.reminder_title))
                .setContentText(context.getResources().getString(R.string.reminder_text))
                .setSmallIcon(R.drawable.ic_notifications_active_white_24dp)
                .setContentIntent(pending)
                .setChannelId(channelID).build();

        NotificationManager nm = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = nm.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelID, context.getResources().getString(R.string.diabuddy_reminders), importance);
        nm.createNotificationChannel(channel);

        nm.notify(notifID, notification);
    }

    private DocumentReference addMessageToGroup(int type, String msg, int group) {
        messagesDB.document("group" + group).update("content", FieldValue.arrayUnion(msg));
        messagesDB.document("group" + group).update("type", FieldValue.arrayUnion(type));
        return messagesDB.document("group" + group);
    }

}