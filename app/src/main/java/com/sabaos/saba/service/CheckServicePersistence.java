package com.sabaos.saba.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sabaos.saba.R;


public class CheckServicePersistence extends IntentService {


    public CheckServicePersistence() {
        super("CheckServicePersistence");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        // This is necessary for service to run, otherwise Android will destroy it after 5 secs.
        NotificationManager notificationManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My_channel";
            String description = "Saba_channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("123", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Saba Market")
                    .setContentText("Initializing")
                    .setAutoCancel(false);
            startForeground(1, builder.build());
        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Saba Market")
                    .setContentText("Initializing")
                    .setAutoCancel(false);
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
            startForeground(1, builder.build());
        }


        if (!isMyServiceRunning(WebSocketService.class)) restartService();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void restartService() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Thread thread = new Thread() {
                public void run() {
                    getApplicationContext().startForegroundService(new Intent(getApplicationContext(), WebSocketService.class));
                }
            };
            thread.start();
        } else {
            Thread thread = new Thread() {
                public void run() {
                    getApplicationContext().startService(new Intent(getApplicationContext(), WebSocketService.class));
                }
            };
            thread.start();
        }
    }

}
