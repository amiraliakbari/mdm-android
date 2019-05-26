package com.sabaos.core;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sabaos.core.Utils.DeviceInfo;
import com.sabaos.core.service.MDMService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class MainActivity extends AppCompatActivity {

    int jobId = 1;
    TextView appVersionV;
    TextView osVersionV;
    TextView phoneSerialV;
    TextView iMEIV;
    TextView phoneNameV;
    TextView osLevelV;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
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
        if ((phoneSerialV != null) && stringContainsNumber(deviceInfo1.getPhoneSerialNumber()))
            phoneSerialV.setText(deviceInfo1.getPhoneSerialNumber());
        else {
            TextView phoneSerialF = (TextView) findViewById(R.id.phoneSerialF);
            phoneSerialF.setVisibility(View.INVISIBLE);
            phoneSerialV.setVisibility(View.INVISIBLE);
        }
        if (iMEIV != null) iMEIV.setText(deviceInfo1.getfirstIMEI(getApplicationContext()));
        if (phoneNameV != null) phoneNameV.setText(deviceInfo1.getPhoneModel());
        if (osLevelV != null)
            osLevelV.setText(deviceInfo1.getOsSecurityLevel(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void callSupport(View view) {

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:+982161975600"));
        startActivity(callIntent);
    }

    public boolean stringContainsNumber(String s) {
        return Pattern.compile("[0-9]").matcher(s).find();
    }

    public void sendMessage(View view) {

        editText = (EditText) findViewById(R.id.editText);
        Log.i("text1", editText.getText().toString());
        String messsageString = "{" +
                "'type':'push'," +
                "'app':'" + editText.getText().toString() + "'," +
                "'message':'This message was meant for SabaOS market'" +
                "}";


        OkHttpClient client = new OkHttpClient.Builder().pingInterval(4, TimeUnit.SECONDS).connectTimeout(1, TimeUnit.DAYS).build();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);

                webSocket.send(messsageString);

                Log.i("WebSocket", "opened!");
                Log.i("WebSocket", "sent push for SabaOS Market");
                Log.i("WebSocket Received", String.valueOf(client.pingIntervalMillis()));
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
//                super.onMessage(webSocket, text);
                Log.i("Received1", text);
                try {
                    Log.i("text", text);
                    JSONObject jsonObject = new JSONObject(text);
                    String app = jsonObject.getString("app");
                    if (app.equalsIgnoreCase("market")) {
                        Log.i("true", "true");
                        Intent intent = new Intent();
                        intent.putExtra("message", "Push notification from Market");
                        intent.setAction("com.sabaos.testmarketapp");
                        intent.setComponent(new ComponentName("com.sabaos.testmarketapp", "com.sabaos.testmarketapp.MyService"));
                        if (Build.VERSION.SDK_INT >= 26) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                    } else if (app.equalsIgnoreCase("riot")) {

                        Intent intent = new Intent();
                        intent.putExtra("message", "Push notification from Riot");
                        intent.setComponent(new ComponentName("com.sabaos.testriotapp", "com.sabaos.testriotapp.MyService"));
                        if (Build.VERSION.SDK_INT >= 26) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                    } else
                        Toast.makeText(getApplicationContext(), "Error! Input should be either market or riot", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.i("Main Activity WebSocket", "");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.i("Main Activity WebSocket", "");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//                super.onFailure(webSocket, t, response);
                Log.i("Main Activity WebSocket", "");

            }
        };
        WebSocket ws = client.newWebSocket(request, listener);

    }
}
