package com.sabaos.core;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sabaos.core.Utils.DeviceInfo;
import com.sabaos.core.service.MDMService;
import com.sabaos.core.service.WebSocketService;
import com.sabaos.messaging.client.ApiClient;
import com.sabaos.messaging.client.api.ClientApi;
import com.sabaos.messaging.client.api.UserApi;
import com.sabaos.messaging.client.model.Client;
import com.sabaos.messaging.client.model.User;
import com.sabaos.messaging.client.model.VersionInfo;
import com.sabaos.messaging.messaging.NotificationSupport;
import com.sabaos.messaging.messaging.SSLSettings;
import com.sabaos.messaging.messaging.Settings;
import com.sabaos.messaging.messaging.api.ApiException;
import com.sabaos.messaging.messaging.api.Callback;
import com.sabaos.messaging.messaging.api.ClientFactory;
import com.sabaos.messaging.messaging.log.UncaughtExceptionHandler;

import static com.sabaos.messaging.messaging.api.Callback.callInUI;


public class MainActivity extends AppCompatActivity {

    private Settings settings;
    private final int FILE_SELECT_CODE = 1;
    private boolean disableSSLValidation;
    private String caCertContents;
    int jobId = 1;
    TextView appVersionV;
    TextView osVersionV;
    TextView phoneSerialV;
    TextView iMEIV;
    TextView phoneNameV;
    TextView osLevelV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationSupport.createChannels(
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));
        }

        UncaughtExceptionHandler.registerCurrentThread();
        com.sabaos.messaging.messaging.log.Log.i("Entering " + getClass().getSimpleName());
        Thread thread = new Thread() {
            public void run() {
                initializeService();
            }
        };
        thread.start();
        checkPermission();
//        DeviceInfo deviceInfo1 = new DeviceInfo();
//        Toast.makeText(getApplicationContext(), String.valueOf(TrafficStats.getTotalRxBytes()), Toast.LENGTH_LONG).show();

    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23) {

            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {

                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE}, 123);
            } else {
                runJob();
                viewParameters();
            }
        } else {
            runJob();
            viewParameters();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 123:
                if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, "Application launched successfully", Toast.LENGTH_LONG).show();
                    Log.i("toasting ", "toasting");
                    runJob();
                    viewParameters();
                } else {
                    Toast.makeText(this, "Application cannot run correctly", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void runJob() {

        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(this, MDMService.class));

        builder.setPeriodic(21600000);
        builder.setPersisted(true);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public void viewParameters() {

        DeviceInfo deviceInfo1 = new DeviceInfo();
        appVersionV = (TextView) findViewById(R.id.appVerV);
        osVersionV = (TextView) findViewById(R.id.osV);
        phoneSerialV = (TextView) findViewById(R.id.phoneSerialV);
        iMEIV = (TextView) findViewById(R.id.imeiV);
        phoneNameV = (TextView) findViewById(R.id.phoneModelV);
        osLevelV = (TextView) findViewById(R.id.osLevelV);
        if (appVersionV != null) appVersionV.setText(deviceInfo1.getApplicationVersion());
        if (osVersionV != null) osVersionV.setText(deviceInfo1.getOsName());
        if (phoneSerialV != null) phoneSerialV.setText(deviceInfo1.getPhoneSerialNumber());
        if (iMEIV != null) iMEIV.setText(deviceInfo1.getfirstIMEI(getApplicationContext()));
        if (phoneNameV != null) phoneNameV.setText(deviceInfo1.getPhoneModel());
        if (osLevelV != null) osLevelV.setText(deviceInfo1.getOsSecurityLevel(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void initializeService() {

        settings = new Settings(this);
        if (settings.tokenExists()) {
            tryAuthenticate();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        doCheckUrl();
        onValidUrl("https://push.sabaos.com");
        doLogin();

    }

    private void tryAuthenticate() {
        ClientFactory.userApiWithToken(settings)
                .currentUser()
                .enqueue(callInUI(this, this::authenticated, this::failed));
    }

    private void failed(ApiException exception) {
        if (exception.code() == 0) {
            return;
        }

        if (exception.code() == 401) {
            return;
        }

        String response = exception.body();
        response = response.substring(0, Math.min(200, response.length()));
    }

//    private void dialog(String message) {
//        new AlertDialog.Builder(this)
//                .setTitle(R.string.oops)
//                .setMessage(message)
//                .setPositiveButton(R.string.retry, (a, b) -> tryAuthenticate())
//                .setNegativeButton(R.string.logout, (a, b) -> showLogin())
//                .show();
//    }

    private void authenticated(User user) {
        com.sabaos.messaging.messaging.log.Log.i("Authenticated as " + user.getName());

        settings.user(user.getName(), user.isAdmin());
        requestVersion(
                () -> {
//                    startActivity(new Intent(this, MainActivity.class));
//                    finish();
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
                    com.sabaos.messaging.messaging.log.Log.i("Server version: " + version.getVersion() + "@" + version.getBuildDate());
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

    public void doCheckUrl() {
        String url = "https://push.sabaos.com";

        final String fixedUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

        ClientFactory.versionApi(fixedUrl, tempSSLSettings())
                .getVersion();
    }

    public void onValidUrl(String url) {
        settings.url(url);
    }

    private Callback.ErrorCallback onInvalidUrl(String url) {
        return (exception) -> {
        };
    }

    public void doLogin() {
        String username = "admin";
        String password = "admin";

        ApiClient client =
                ClientFactory.basicAuth(settings.url(), tempSSLSettings(), username, password);
        client.createService(UserApi.class)
                .currentUser()
                .enqueue(callInUI(this, (user) -> newClientDialog(client), this::onInvalidLogin));
    }

    private void onInvalidLogin(ApiException e) {
    }

    private void newClientDialog(ApiClient client) {
        EditText clientName = new EditText(this);
        clientName.setText(Build.MODEL);
        doCreateClient(client, clientName);
    }

    public void doCreateClient(ApiClient client, EditText nameProvider) {

        Client newClient = new Client().name(nameProvider.getText().toString());
        client.createService(ClientApi.class)
                .createClient(newClient)
                .enqueue(callInUI(this, this::onCreatedClient, this::onFailedToCreateClient));

    }


    private void onCreatedClient(Client client) {
        settings.token(client.getToken());
        settings.validateSSL(!disableSSLValidation);
        settings.cert(caCertContents);
        initializeService();
    }

    private void onFailedToCreateClient(ApiException e) {
    }

    private void onCancelClientDialog(DialogInterface dialog, int which) {
    }

//    private String versionError(String url, ApiException exception) {
//        return getString(R.string.version_failed, url + "/version", exception.code());
//    }

    private SSLSettings tempSSLSettings() {
        return new SSLSettings(!disableSSLValidation, caCertContents);
    }

    public void callSupport(View view) {

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:+982161975600"));
        startActivity(callIntent);
    }
}
