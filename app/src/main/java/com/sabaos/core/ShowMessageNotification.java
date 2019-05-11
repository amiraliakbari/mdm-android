package com.sabaos.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.sabaos.messaging.messaging.NotificationSupport;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class ShowMessageNotification {

    public void showNotification(Context context, int id, String title, String message, long priority) {

        NotificationCompat.Builder b =
                new NotificationCompat.Builder(
                        context, NotificationSupport.convertPriorityToChannel(priority));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            showNotificationGroup(context, priority);
        }

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_saba)
                .setTicker(context.getString(R.string.app_name) + " - " + title)
                .setGroup(NotificationSupport.Group.MESSAGES)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setLights(Color.CYAN, 1000, 5000)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, b.build());
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(1000);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public void showNotificationGroup(Context context, long priority) {

        NotificationCompat.Builder b =
                new NotificationCompat.Builder(
                        context, NotificationSupport.convertPriorityToChannel(priority));

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_saba)
                .setTicker(context.getString(R.string.app_name))
                .setGroup(NotificationSupport.Group.MESSAGES)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setContentTitle(context.getString(R.string.grouped_notification_text))
                .setGroupSummary(true)
                .setContentText(context.getString(R.string.grouped_notification_text))
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(-5, b.build());
    }
}
