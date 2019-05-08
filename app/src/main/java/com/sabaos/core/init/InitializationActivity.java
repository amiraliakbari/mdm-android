package com.sabaos.core.init;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sabaos.core.MainActivity;
import com.sabaos.core.R;
import com.sabaos.core.login.LoginActivity;
import com.sabaos.core.service.WebSocketService;
import com.sabaos.messaging.client.model.User;
import com.sabaos.messaging.client.model.VersionInfo;
import com.sabaos.messaging.messaging.NotificationSupport;
import com.sabaos.messaging.messaging.Settings;
import com.sabaos.messaging.messaging.api.ApiException;
import com.sabaos.messaging.messaging.api.Callback;
import com.sabaos.messaging.messaging.api.ClientFactory;
import com.sabaos.messaging.messaging.log.Log;
import com.sabaos.messaging.messaging.log.UncaughtExceptionHandler;

import static com.sabaos.messaging.messaging.api.Callback.callInUI;

public class InitializationActivity extends AppCompatActivity {
    private Settings settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.init(this);
        setContentView(R.layout.splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationSupport.createChannels(
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));
        }

        UncaughtExceptionHandler.registerCurrentThread();
        settings = new Settings(this);
        Log.i("Entering " + getClass().getSimpleName());
        if (settings.tokenExists()) {
            tryAuthenticate();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void tryAuthenticate() {
        ClientFactory.userApiWithToken(settings)
                .currentUser()
                .enqueue(callInUI(this, this::authenticated, this::failed));
    }

    private void failed(ApiException exception) {
        if (exception.code() == 0) {
//            dialog(getString(R.string.not_available, settings.url()));
            return;
        }

        if (exception.code() == 401) {
//            dialog(getString(R.string.auth_failed));
            return;
        }

        String response = exception.body();
        response = response.substring(0, Math.min(200, response.length()));
//        dialog(getString(R.string.other_error, settings.url(), exception.code(), response));
    }

    private void dialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.oops)
                .setMessage(message)
                .setPositiveButton(R.string.retry, (a, b) -> tryAuthenticate())
                .setNegativeButton(R.string.logout, (a, b) -> showLogin())
                .show();
    }

    private void authenticated(User user) {
        Log.i("Authenticated as " + user.getName());

        settings.user(user.getName(), user.isAdmin());
        requestVersion(
                () -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, WebSocketService.class));
        } else {
            startService(new Intent(this, WebSocketService.class));
        }
    }

    private void requestVersion(Runnable runnable) {
        requestVersion(
                (version) -> {
                    Log.i("Server version: " + version.getVersion() + "@" + version.getBuildDate());
                    settings.serverVersion(version.getVersion());
                    runnable.run();
                },
                (e) -> {
                    runnable.run();
                });
    }

    private void requestVersion(
            final Callback.SuccessCallback<VersionInfo> callback,
            final Callback.ErrorCallback errorCallback) {
        ClientFactory.versionApi(settings.url(), settings.sslSettings())
                .getVersion()
                .enqueue(callInUI(this, callback, errorCallback));
    }
}
