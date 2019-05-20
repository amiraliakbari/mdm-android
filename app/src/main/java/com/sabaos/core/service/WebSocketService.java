package com.sabaos.core.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sabaos.core.R;
import com.sabaos.core.Utils.DBManager;
import com.sabaos.core.Utils.DeviceInfo;
import com.sabaos.core.Utils.SharedPref;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        caculateMobileData();
        saveMobileDataInSQLite();
//        new Thread(this::foreground).run();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                foreground();
            }
        }, 0, 1000);

        return START_STICKY;
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

        if (Build.VERSION.SDK_INT >= 26){

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


        String url = "{\"v\": " + deviceInfo.getApplicationVersion() + ", phoneid=" + deviceInfo.getPhoneSerialNumber() + "&hwid=" + deviceInfo.getHWSerialNumber() +
                deviceInfo.getIMEI(getApplicationContext()) + "}";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                webSocket.send(url);
                Log.i("WebSocket Received " ,"opened!");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.i("WebSocket Received " ,text);
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
                super.onFailure(webSocket, t, response);
                Log.i("WebSocket failed", "");
            }
        };
        WebSocket ws = client.newWebSocket(request, listener);
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
                networkTraffic = currentValue - firstValue;
                count += networkTraffic;
                firstValue = currentValue;
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
}
