package com.example.groceriesmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        // notification compat builder for constructing the details of the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyExpiryDate")
                .setContentTitle("Pantry item expiring soon!")
                .setContentText("Open the app to find recipes for " + name)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

//        .setSmallIcon(R.drawable.fresh_fruit)
        // todo: set pending intent for searching recipes: possibly first search user saved recipes
//                .addAction(android.R.drawable.ic_search_category_default, "find recipes!",
//                        pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // todo: set this as a transparent
            builder.setSmallIcon(R.drawable.app_icon_transparent);
            builder.setColor(context.getResources().getColor(R.color.white));
        } else {
            builder.setSmallIcon(R.drawable.app_icon);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // todo: change id to unique id for each notification and find a way to store it since you need it to remove or edit notification
        // source: https://developer.android.com/training/notify-user/build-notification
        notificationManager.notify(200, builder.build());
    }
}
