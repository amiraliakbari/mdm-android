package com.sabaos.saba.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.sabaos.saba.R;
import com.sabaos.saba.utils.RegisterApp;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.sabaos.saba.service.action.FOO";
    private static final String ACTION_BAZ = "com.sabaos.saba.service.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.sabaos.saba.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.sabaos.saba.service.extra.PARAM2";

    public MyIntentService() {
        super("MyIntentService");
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
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


        Log.i("Service1 ", "Started!");
        Log.i("Service1 ", "intent received");
        //checks type of message and executes corresponding action
        Bundle bundle = intent.getExtras();
        String type = bundle.getString("type");
        if (type.equalsIgnoreCase("registerApp")) {

            String app = bundle.getString("app");
            RegisterApp registerApp = new RegisterApp(getApplicationContext(), app);
            registerApp.registerApp1();
        }

    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
