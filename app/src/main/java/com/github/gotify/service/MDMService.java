package com.github.gotify.service;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Method;

public class MDMService extends JobService {

    // This value keeps application version.
    String appVersion = "0.1";
    // This value will keep serial number of the device
    String serialNumber;

    // starts each time the service runs
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        // retrieves device serial number from SystemProperties in a safe manner
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);

            serialNumber = (String) get.invoke(c, "sys.serialnumber", "error");
            if (serialNumber.equals("error")) {
                serialNumber = (String) get.invoke(c, "ril.serialnumber", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // creates a queue using Volley library for holding HTTP requests
        RequestQueue updatequeue = Volley.newRequestQueue(this);
        // defines the URL as a String and adds device ID and application version to the String
        String url = "https://sabaos.com/mdm/collect.gif?" + "deviceID=" + serialNumber + "?appVerion=" + appVersion;
        // Creates a Get request instance from StringRequest class
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
