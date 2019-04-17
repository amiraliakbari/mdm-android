package com.example.sabacloudmessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ShowMessageNotification {

    public void viewNotification(String title, String message, Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "default description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.default_notification_channel_id), "Channel human readable title", importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_saba)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int id = (int) System.currentTimeMillis();
        notificationManager.notify(id, builder.build());
    }
}
