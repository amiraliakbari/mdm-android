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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sabaos.core.Utils.DeviceInfo;
import com.sabaos.core.service.MDMService;


public class MainActivity extends AppCompatActivity {

    int jobId = 1;
    TextView appVersionF;
    TextView appVersionV;
    TextView osVersionV;
    TextView osVersionF;
    TextView phoneSerialF;
    TextView phoneSerialV;
    TextView iMEIF;
    TextView iMEIV;
    TextView phoneNameF;
    TextView phoneNameV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();






    }

    private void checkPermission(){

        if (Build.VERSION.SDK_INT >= 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 123);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        switch (requestCode){

            case 123: if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    Toast.makeText(this, "Application launched successfully", Toast.LENGTH_LONG).show();
                Log.i("toasting ", "toasting");
                runJob();
                viewParameters();
            }else {
                Toast.makeText(this, "Application cannot run correctly", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void runJob(){

        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(this, MDMService.class));

        builder.setOverrideDeadline(10000);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public void viewParameters(){

        DeviceInfo deviceInfo1 = new DeviceInfo();
        appVersionF = (TextView)findViewById(R.id.appVerF);
        appVersionV = (TextView)findViewById(R.id.appVerV);
        osVersionV =  (TextView)findViewById(R.id.osV);
        osVersionF =  (TextView)findViewById(R.id.osF);
        phoneSerialF = (TextView) findViewById(R.id.phoneSerialF);
        phoneSerialV = (TextView) findViewById(R.id.phoneSerialV);
        iMEIF = (TextView) findViewById(R.id.imeiF);
        iMEIV = (TextView) findViewById(R.id.imeiV);
        phoneNameF = (TextView) findViewById(R.id.phoneModelF);
        phoneNameV = (TextView) findViewById(R.id.phoneModelV);
        if (appVersionF != null) appVersionF.setText("Application version: ");
        if (appVersionV != null) appVersionV.setText(deviceInfo1.getApplicationVersion());
        if (osVersionF != null) osVersionF.setText("OS version: ");
        if (osVersionV != null) osVersionV.setText(deviceInfo1.getOsName());
        if (phoneSerialF != null) phoneSerialF.setText("Serial number: ");
        if (phoneSerialV != null) phoneSerialV.setText(deviceInfo1.getPhoneSerialNumber());
        if (iMEIF != null) iMEIF.setText("IMEI: ");
        if(iMEIV != null) iMEIV.setText(deviceInfo1.getfirstIMEI(getApplicationContext()));
        if (phoneNameF != null) phoneNameF.setText("Model: ");
        if (phoneNameV != null) phoneNameV.setText(deviceInfo1.getPhoneModel());
    }

    public void goHome(View view) {

    }

    public void goMemory(View view) {



    }

    public void goBattery(View view) {




    }

    public void goMoblieData(View view) {




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

}
