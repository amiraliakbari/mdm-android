package com.sabaos.saba.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import static com.sabaos.saba.service.WebSocketService.sharedPref;
import static com.sabaos.saba.service.WebSocketService.ws;

public class RegisterApp {

    //Creates two timers that are going to send registration Tokens to push server until a successful result is
    //returned which sets the "IsregisteredMarket/Riot" to true and stops timers. That code is in Message Handle class.
    public static Timer marketTimer = new Timer();
    public static Timer riotTimer = new Timer();
    private Context context;
    String packageName;

    public RegisterApp(Context context, String packageName) {

        this.context = context;
        this.packageName = packageName;
    }

    public void registerApp1() {


        //if it's intended for Market app the following code will run
        if (packageName.equalsIgnoreCase("com.sabaos.testmarketapp")) {
            //checks to see if the app is running for the first time, if so, it will generate and
            //save the token
            if (sharedPref.loadData("marketToken").equals("empty")) {
                //Build token
                String marketToken =  new SabaSecureRandom().generateSecureRandom();
                Log.i("creating market token", "for the first time");
                sharedPref.saveData("marketToken", marketToken);
                sharedPref.saveData("IsMarketRegistered", "false");
            }

            //send Token back to app
            Intent intent = new Intent();
            intent.putExtra("type", "registerApp");
            intent.putExtra("marketToken", sharedPref.loadData("marketToken"));
            intent.putExtra("packageName", packageName);
            intent.setComponent(new ComponentName("com.sabaos.testmarketapp", "com.sabaos.testmarketapp.MyIntentService"));
            if (Build.VERSION.SDK_INT >= 26) {
                Log.i("sending token", "back to market");
                context.startForegroundService(intent);
            } else context.startService(intent);

            //send Token to push server
            String registerApp = "{\"type\":\"registerApp\",\"app\":\"com.sabaos.testmarketapp\", \"token\":\"" + sharedPref.loadData("marketToken") + "\"}";
            Log.i("after websocket", "sent registerApp");
            Log.i("token value", sharedPref.loadData("IsMarketRegistered"));
            ws.send(registerApp);
            marketTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (sharedPref.loadData("IsMarketRegistered").equalsIgnoreCase("false")){
                        ws.send(registerApp);
                        Log.i("retrying","to send token to server");
                    }
                }
            }, 0, 60000);


            //if it's intended for Riot app the following code will run
        } else if (packageName.equalsIgnoreCase("com.sabaos.testriotapp")) {
            //checks to see if the app is running for the first time, if so it will generate and
            //save the token
            if (sharedPref.loadData("riotToken").equals("empty")) {
                //Build token
                String riotToken =  new SabaSecureRandom().generateSecureRandom();
                sharedPref.saveData("riotToken", riotToken);
                sharedPref.saveData("IsRiotRegistered", "false");
            }
            //send Token back to app
            Intent intent = new Intent();
            intent.putExtra("type", "registerApp");
            intent.putExtra("riotToken", "riotToken");
            intent.putExtra("packageName", packageName);
            intent.setComponent(new ComponentName("com.sabaos.testriotapp", "com.sabaos.testriotapp.MyIntentService"));
            if (Build.VERSION.SDK_INT >= 26) {

                context.startForegroundService(intent);
            } else context.startService(intent);

            //send Token to push server
            String registerApp = "{\"type\":\"registerApp\",\"app\":\"com.sabaos.testriotapp\", \"token\":\"" + sharedPref.loadData("riotToken") + "\"}";
            ws.send(registerApp);
            riotTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (sharedPref.loadData("IsRiotRegistered").equalsIgnoreCase("false")){
                        ws.send(registerApp);
                        Log.i("retrying","to send token to server");
                    }
                }
            }, 0, 51000);

        }
    }

}
