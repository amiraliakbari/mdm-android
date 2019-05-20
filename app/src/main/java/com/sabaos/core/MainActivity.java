package com.sabaos.core;

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
import android.widget.TextView;
import android.widget.Toast;

import com.sabaos.core.Utils.DeviceInfo;
import com.sabaos.core.service.MDMService;

import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    //    private Settings settings;
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
}
