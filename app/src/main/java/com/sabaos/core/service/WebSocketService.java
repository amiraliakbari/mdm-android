package com.sabaos.core.service;

import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sabaos.core.R;
import com.sabaos.core.Utils.DBManager;
import com.sabaos.core.Utils.DeviceInfo;
import com.sabaos.core.Utils.SharedPref;
import com.sabaos.messaging.client.api.MessageApi;
import com.sabaos.messaging.client.model.Message;
import com.sabaos.messaging.messaging.MissedMessageUtil;
import com.sabaos.messaging.messaging.NotificationSupport;
import com.sabaos.messaging.messaging.Settings;
import com.sabaos.messaging.messaging.Utils;
import com.sabaos.messaging.messaging.api.ClientFactory;
import com.sabaos.messaging.messaging.log.Log;
import com.sabaos.messaging.messaging.log.UncaughtExceptionHandler;
import com.sabaos.messaging.messaging.service.ReconnectListener;
import com.sabaos.messaging.messaging.service.WebSocketConnection;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketService extends Service {


    public static final String NEW_MESSAGE_BROADCAST =
            WebSocketService.class.getName() + ".NEW_MESSAGE";

    private static final int NOT_LOADED = -2;

    private Settings settings;
    private WebSocketConnection connection;

    private AtomicInteger lastReceivedMessage = new AtomicInteger(NOT_LOADED);
    private MissedMessageUtil missingMessageUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        settings = new Settings(this);
        missingMessageUtil =
                new MissedMessageUtil(
                        ClientFactory.clientToken(
                                settings.url(), settings.sslSettings(), settings.token())
                                .createService(MessageApi.class));
        Log.i("Create " + getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            connection.close();
        }
        Log.w("Destroy " + getClass().getSimpleName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.init(this);

        if (connection != null) {
            connection.close();
        }

        Log.i("Starting " + getClass().getSimpleName());
        super.onStartCommand(intent, flags, startId);
        caculateMobileData();
        saveMobileDataInSQLite();
        new Thread(this::startPushService).run();


        return START_STICKY;
    }

    private void startPushService() {
        UncaughtExceptionHandler.registerCurrentThread();

        if (lastReceivedMessage.get() == NOT_LOADED) {
            missingMessageUtil.lastReceivedMessage(lastReceivedMessage::set);
        }
        foreground();
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        connection =
                new WebSocketConnection(
                        settings.url(), settings.sslSettings(), settings.token(), cm)
                        .onOpen(this::onOpen)
                        .onClose(() -> foreground())
//                        .onBadRequest(this::onBadRequest)
                        .onNetworkFailure(
                                (min) -> foreground())
                        .onDisconnect(this::onDisconnect)
                        .onMessage(this::onMessage)
                        .onReconnected(this::notifyMissedNotifications)
                        .start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ReconnectListener receiver = new ReconnectListener(this::doReconnect);
        registerReceiver(receiver, intentFilter);
    }

    private void onDisconnect() {
        foreground();
    }

    private void doReconnect() {
        if (connection == null) {
            return;
        }

        connection.scheduleReconnect(TimeUnit.SECONDS.toMillis(5));
    }

    private void onBadRequest() {
        foreground();
    }

    private void onOpen() {
        foreground();
    }

    private void notifyMissedNotifications() {
        int messageId = lastReceivedMessage.get();
        if (messageId == NOT_LOADED) {
            return;
        }

        List<Message> messages = missingMessageUtil.missingMessages(messageId);

        if (messages.size() > 5) {
            onGroupedMessages(messages);
        } else {
            for (Message message : messages) {
                onMessage(message);
            }
        }
    }

    private void onGroupedMessages(List<Message> messages) {
        long highestPriority = 0;
        for (Message message : messages) {
            if (lastReceivedMessage.get() < message.getId()) {
                lastReceivedMessage.set(message.getId());
                highestPriority = Math.max(highestPriority, message.getPriority());
            }
            broadcast(message);
        }
        int size = messages.size();
    }

    private void onMessage(Message message) {
        if (lastReceivedMessage.get() < message.getId()) {
            lastReceivedMessage.set(message.getId());
        }
        broadcast(message);
    }

    private void broadcast(Message message) {
        Intent intent = new Intent();
        intent.setAction(NEW_MESSAGE_BROADCAST);
        intent.putExtra("message", Utils.JSON.toJson(message));
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void foreground() {

        SharedPref sharedPref = new SharedPref(getApplicationContext());
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);
        DeviceInfo deviceInfo = new DeviceInfo();
        String memoryStatus = getString(R.string.memory_string) + " " + deviceInfo.showUsedMemory(getApplicationContext());
        notificationLayout.setProgressBar(R.id.progressBar1, 100, deviceInfo.showProgressValue(getApplicationContext()), false);
        notificationLayoutExpanded.setProgressBar(R.id.progressBar1, 100, deviceInfo.showProgressValue(getApplicationContext()), false);
        notificationLayout.setTextViewText(R.id.textView1, memoryStatus);
        notificationLayoutExpanded.setTextViewText(R.id.textView1, memoryStatus);
        notificationLayout.setTextViewText(R.id.textView2, getFormattedTraffic(new Long(sharedPref.loadData("count")).longValue()));
        notificationLayoutExpanded.setTextViewText(R.id.textView2, getFormattedTraffic(new Long(sharedPref.loadData("count")).longValue()));

        Notification notification =

                new NotificationCompat.Builder(this, NotificationSupport.Channel.FOREGROUND)
                        .setSmallIcon(R.mipmap.ic_saba)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(notificationLayout)
                        .setCustomBigContentView(notificationLayoutExpanded)
                        .build();

        startForeground(NotificationSupport.ID.FOREGROUND, notification);
    }

    public String getFormattedTraffic(long dataLong) {

        String dataString = "";
        if (dataLong < 1000) {
            dataString = getString(R.string.data_string) + " " + new Double(dataLong).toString() + "B";
            return dataString;
        } else if (dataLong < 1000000) {
            double value = dataLong / 1000.0;
            BigDecimal bd1 = new BigDecimal(value).round(new MathContext(3));
            dataString = getString(R.string.data_string) + " " + bd1.toString() + "KB";
            return dataString;
        } else if (dataLong < 1000000000) {
            double value = dataLong / 1000000.0;
            BigDecimal bd1 = new BigDecimal(value).round(new MathContext(3));
            dataString = getString(R.string.data_string) + " " + bd1.toString() + "MB";
            return dataString;
        } else {
            double value = dataLong / 1000000000.0;
            BigDecimal bd1 = new BigDecimal(value).round(new MathContext(4));
            dataString = getString(R.string.data_string) + " " + bd1.toString() + "GB";
            return dataString;
        }
    }

    private void caculateMobileData() {

        new Timer().schedule(new TimerTask() {

            SharedPref sharedPref = new SharedPref(getApplicationContext());
            long count = 0;
            long firstValue = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
            long networkTraffic = 0;

            @Override
            public void run() {
                if (sharedPref.loadData("count").equals("empty")) {

                    sharedPref.saveData("count", "0");
                } else {
                    String value = sharedPref.loadData("count");
                    count = new Long(value).longValue();
                    Log.i("count: " + count);
                }
                Log.i("runnable running");
                long currentValue = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
                networkTraffic = currentValue - firstValue;
                Log.i("network Traffic: " + networkTraffic);
                count += networkTraffic;
                firstValue = currentValue;
                sharedPref.saveData("count", String.valueOf(count));
                Log.i("time " + Calendar.getInstance().getTime());
                Log.i("initial value" + sharedPref.loadData("count"));
            }
        }, 0, 1000);
    }


    public void saveMobileDataInSQLite() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPref sharedPref = new SharedPref(getApplicationContext());
                DBManager dbManager = new DBManager(getApplicationContext());
                ContentValues values = new ContentValues();
                values.put("date", Calendar.getInstance().getTime().toString());
                values.put("data", sharedPref.loadData("count"));
                long id = dbManager.insert(values);
                Log.i("DB returned: " + id);
            }
        }, 0, 60000);
    }
}
