package com.example.groceriesmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // notification compat builder for constructing the details of the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyExpiryDate")
                .setContentTitle("Some of your pantry items are about to expire")
                .setContentText("text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(200, builder.build() );
    }
}
