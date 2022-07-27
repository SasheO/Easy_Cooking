package com.example.groceriesmanager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.groceriesmanager.Activities.EditFoodItemActivity;
import com.example.groceriesmanager.Activities.MainActivity;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // cancel old intent and create new one
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, flags);

        String name = intent.getStringExtra("name");
        String fragment = intent.getStringExtra("fragment");
        // notification compat builder for constructing the details of the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyExpiryDate")
                .setContentTitle("Pantry item expiring soon!")
                .setContentText("Open the app to find recipes for " + name)
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

//        .setSmallIcon(R.drawable.fresh_fruit)
        // todo: set pending intent for searching recipes: possibly first search user saved recipes
//        // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);

        // the small icon must be set this way bc of https://stackoverflow.com/questions/30795431/android-push-notifications-icon-not-displaying-in-notification-white-square-sh
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
