package com.sabaos.core.init;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sabaos.core.service.MDMService;
import com.sabaos.core.service.WebSocketService;


public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int jobId = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Thread thread = new Thread() {
                public void run() {
                    context.startForegroundService(new Intent(context, WebSocketService.class));
                }
            };
            thread.start();
        } else {
            Thread thread = new Thread() {
                public void run() {
                    context.startService(new Intent(context, WebSocketService.class));
                }
            };
            thread.start();
        }

        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(context, MDMService.class));

        builder.setPeriodic(21600000);
        builder.setPersisted(true);

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
}
