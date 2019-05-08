package com.sabaos.core.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sabaos.core.Utils.DeviceInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MDMService extends JobService {


    // This value will keep serial number of the device
    // starts each time the service runs
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.i("now1 ", "starting the job service");

        RequestQueue updatequeue = Volley.newRequestQueue(this);

        DeviceInfo deviceInfo =  new DeviceInfo();
        String url = "https://sabaos.com/mdm/collect.gif?" + "v=" + deviceInfo.getApplicationVersion() + "&phoneid=" + deviceInfo.getPhoneSerialNumber() + "&hwid=" + deviceInfo.getHWSerialNumber() +
                deviceInfo.getIMEI(getApplicationContext());
        // Creates a Get request instance from StringRequest class
        Log.i("url: ", url);
        StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //if the server responds back then we're done
                jobFinished(jobParameters, false);
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if an error occurs
                jobFinished(jobParameters, true);
            }
        });
        // adds the Get request to the queue for execution
        updatequeue.add(ExampleStringRequest);
        return false;
    }


    @Override
    // if for any reason the job stopped it will be reschedules to run ASAP
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }


}



