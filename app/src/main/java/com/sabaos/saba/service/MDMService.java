package com.sabaos.saba.service;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sabaos.saba.utils.DeviceInfo;


public class MDMService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        RequestQueue updatequeue = Volley.newRequestQueue(this);
        DeviceInfo deviceInfo = new DeviceInfo(getApplicationContext());
        String url = "https://sabaos.com/mdm/collect.gif?" + "v=" + deviceInfo.getApplicationVersion() + "&phoneid=" + deviceInfo.getPhoneSerialNumber() + "&hwid=" + deviceInfo.getHWSerialNumber() +
                deviceInfo.getIMEI();
        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                jobFinished(jobParameters, false);
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                jobFinished(jobParameters, true);
            }
        });
        updatequeue.add(ExampleStringRequest);
        if (!isMyServiceRunning(WebSocketService.class)) restartService();
        return false;
    }


    @Override
    // if for any reason the job stopped it will be reschedules to run ASAP
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
    public void restartService() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Thread thread = new Thread() {
                public void run() {
                    getApplicationContext().startForegroundService(new Intent(getApplicationContext(), WebSocketService.class));
                }
            };
            thread.start();
        } else {
            Thread thread = new Thread() {
                public void run() {
                    getApplicationContext().startService(new Intent(getApplicationContext(), WebSocketService.class));
                }
            };
            thread.start();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}



