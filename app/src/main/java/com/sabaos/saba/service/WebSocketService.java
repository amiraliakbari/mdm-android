package com.sabaos.saba.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sabaos.saba.R;
import com.sabaos.saba.Utils.DBManager;
import com.sabaos.saba.Utils.DeviceInfo;
import com.sabaos.saba.Utils.MessageHandle;
import com.sabaos.saba.Utils.RegisterApp;
import com.sabaos.saba.Utils.SharedPref;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {

    public static WebSocket ws;
    public static SharedPref sharedPref;
    DeviceInfo deviceInfo;
    String deviceId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceInfo = new DeviceInfo(getApplicationContext());
        sharedPref = new SharedPref(getApplicationContext());
        if (sharedPref.loadData("deviceId").equals("empty")) {

            sharedPref.saveData("deviceId", "1234");
        }
        caculateMobileData();
        saveMobileDataInSQLite();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                foreground();
            }
        }, 0, 1000);
        startWebSocket();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void foreground() {

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        String memoryStatus = getString(R.string.memory_string) + " " + deviceInfo.showUsedMemory();
        notificationLayout.setProgressBar(R.id.progressBar1, 100, deviceInfo.showProgressValue(), false);
        notificationLayoutExpanded.setProgressBar(R.id.progressBar1, 100, deviceInfo.showProgressValue(), false);
        notificationLayout.setTextViewText(R.id.textView1, memoryStatus);
        notificationLayoutExpanded.setTextViewText(R.id.textView1, memoryStatus);
        long r = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
        Log.i("count", sharedPref.loadData(String.valueOf(r)));
        notificationLayout.setTextViewText(R.id.textView2, getFormattedTraffic(new Long(sharedPref.loadData("count")).longValue()));
        notificationLayoutExpanded.setTextViewText(R.id.textView2, getFormattedTraffic(new Long(sharedPref.loadData("count")).longValue()));

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "Saba_notification";
            CharSequence channelName = "Saba_channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification notification =

                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_saba)
                            .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                            .setCustomContentView(notificationLayout)
                            .setCustomBigContentView(notificationLayoutExpanded)
                            .build();
            startForeground(importance, notification);
        } else {
            Notification notification =

                    new NotificationCompat.Builder(this, "Saba_notification")
                            .setSmallIcon(R.mipmap.ic_saba)
                            .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                            .setCustomContentView(notificationLayout)
                            .setCustomBigContentView(notificationLayoutExpanded)
                            .build();
            startForeground(NotificationManager.IMPORTANCE_LOW, notification);
        }

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
                }
                long currentValue = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
                if (currentValue >= firstValue) networkTraffic = currentValue - firstValue;
                count += networkTraffic;
                if (currentValue >= firstValue) firstValue = currentValue;
                sharedPref.saveData("count", String.valueOf(count));
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
            }
        }, 0, 60000);
    }

    public void startWebSocket() {
        MessageHandle messageHandle = new MessageHandle();
        Handler handler = new Handler();
        DeviceInfo deviceInfo = new DeviceInfo(getApplicationContext());
        String url = "{\"v\": " + deviceInfo.getApplicationVersion() + ", phoneid=" + deviceInfo.getPhoneSerialNumber() + "&hwid=" + deviceInfo.getHWSerialNumber() +
                deviceInfo.getIMEI() + "}";
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(0, TimeUnit.HOURS).build();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                Log.i("WebSocket ", "opened!");
                String deviceId = "{\"type\":\"register\",\"deviceId\":\"" + sharedPref.loadData("deviceId") + "\"}";
                webSocket.send(deviceId);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.i("WebSocket Received ", text);
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
//                    }
//                });

                messageHandle.handleReceivedMessages(getApplicationContext(), text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.i("WebSocket closing", "");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.i("WebSocket closed", "");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//                super.onFailure(webSocket, t, response);
                Log.i("WebSocket failed", "");
                handler.postDelayed(WebSocketService.this::startWebSocket, 5000);
                Log.i("WebSocket failed", String.valueOf(client.connectTimeoutMillis()));
            }
        };
        ws = client.newWebSocket(request, listener);
    }
}
