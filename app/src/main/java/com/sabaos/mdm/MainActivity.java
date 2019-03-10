package com.sabaos.mdm;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    // defining a job number for JobInfo
    int jobid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}



