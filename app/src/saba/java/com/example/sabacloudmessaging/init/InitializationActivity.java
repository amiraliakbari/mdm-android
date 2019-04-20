package com.example.sabacloudmessaging.init;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.sabacloudmessaging.R;
import com.example.sabacloudmessaging.MainActivity;
import com.example.sabacloudmessaging.NotificationSupport;
import com.example.sabacloudmessaging.Settings;
import com.example.sabacloudmessaging.api.ApiException;
import com.example.sabacloudmessaging.api.Callback;
import com.example.sabacloudmessaging.api.ClientFactory;
import com.example.sabacloudmessaging.log.Log;
import com.example.sabacloudmessaging.log.UncaughtExceptionHandler;
import com.example.sabacloudmessaging.login.LoginActivity;
import com.example.sabacloudmessaging.service.MDMService;
import com.example.sabacloudmessaging.service.WebSocketService;
import com.github.gotify.client.model.User;
import com.github.gotify.client.model.VersionInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.sabacloudmessaging.api.Callback.callInUI;

public class InitializationActivity extends AppCompatActivity {
    private Settings settings;
    // defining a job number for JobInfo
    int jobid = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.init(this);
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
//              settings = new Settings(this);
//              settings.serverVersion("2.0.2");
//              settings.token();
//              settings.user("admin", true);
//              settings.serverVersion("2.0.2");
//              settings.validateSSL(false);


        }
        // Defining the Job information
        JobInfo.Builder builder = new JobInfo.Builder(jobid, new ComponentName(this, MDMService.class));
        // The setPeriodic() method has 2 signatures for before and after SDK 24, so we have to implement both
        if (Build.VERSION.SDK_INT < 24) {
            builder.setPeriodic(10000);
        }
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setPeriodic(10000, 2000);
        }
        // helps service continuum after reboot
        builder.setPersisted(true);
        // doesn't require the device to be idle in order to run the job
        builder.setRequiresDeviceIdle(false);
        // requires a network connection(of any type) to run the job
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        //connects the JobScheduler to System Service
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // Schedules the job with the defined JobInfo which is builder
        jobScheduler.schedule(builder.build());
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
            dialog(getString(R.string.not_available, settings.url()));
            return;
        }

        if (exception.code() == 401) {
            dialog(getString(R.string.auth_failed));
            return;
        }

        String response = exception.body();
        response = response.substring(0, Math.min(200, response.length()));
        dialog(getString(R.string.other_error, settings.url(), exception.code(), response));
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
