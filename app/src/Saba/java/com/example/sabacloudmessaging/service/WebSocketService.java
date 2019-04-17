package com.example.sabacloudmessaging.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.example.sabacloudmessaging.R;
import com.example.sabacloudmessaging.MissedMessageUtil;
import com.example.sabacloudmessaging.NotificationSupport;
import com.example.sabacloudmessaging.Settings;
import com.example.sabacloudmessaging.ShowMessageNotification;
import com.example.sabacloudmessaging.Utils;
import com.example.sabacloudmessaging.api.ClientFactory;
import com.example.sabacloudmessaging.log.Log;
import com.example.sabacloudmessaging.log.UncaughtExceptionHandler;
import com.github.gotify.client.api.MessageApi;
import com.github.gotify.client.model.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class WebSocketService extends Service {

    // ShowMessageNotification class code is in the main part of the application.
    //Creating an instance of it helps us call its function to show received messages as notfications.
    ShowMessageNotification ShowMessage = new ShowMessageNotification();
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
        new Thread(this::startPushService).run();

        return START_STICKY;
    }

    private void startPushService() {
        UncaughtExceptionHandler.registerCurrentThread();
        foreground(getString(R.string.websocket_init));

        if (lastReceivedMessage.get() == NOT_LOADED) {
            missingMessageUtil.lastReceivedMessage(lastReceivedMessage::set);
        }

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        connection =
                new WebSocketConnection(
                        settings.url(), settings.sslSettings(), settings.token(), cm)
                        .onOpen(this::onOpen)
                        .onClose(() -> foreground(getString(R.string.websocket_closed)))
                        .onBadRequest(this::onBadRequest)
                        .onNetworkFailure(
                                (min) -> foreground(getString(R.string.websocket_failed, min)))
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
        foreground(getString(R.string.websocket_no_network));
    }

    private void doReconnect() {
        if (connection == null) {
            return;
        }

        connection.scheduleReconnect(TimeUnit.SECONDS.toMillis(5));
    }

    private void onBadRequest(String message) {
        foreground(getString(R.string.websocket_could_not_connect, message));
    }

    private void onOpen() {
        foreground(getString(R.string.websocket_listening, settings.url()));

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
        //calling viewNotification method in ShowMessageNotification Class to show messages as notifications.
        ShowMessage.viewNotification(getString(R.string.app_name), "Received " + size + " messages while being disconnected", getApplicationContext());

    }

    private void onMessage(Message message) {
        if (lastReceivedMessage.get() < message.getId()) {
            lastReceivedMessage.set(message.getId());
        }
        broadcast(message);
        ShowMessage.viewNotification(getString(R.string.app_name), message.getMessage(), this);

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

    private void foreground(String message) {

        Notification notification =
                new NotificationCompat.Builder(this, NotificationSupport.Channel.FOREGROUND)
                        .setSmallIcon(R.mipmap.ic_saba)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setShowWhen(false)
                        .setWhen(0)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(null)
                        .setColor(
                                ContextCompat.getColor(
                                        getApplicationContext(), R.color.colorPrimary))
                        .build();

        startForeground(NotificationSupport.ID.FOREGROUND, notification);
    }
}
