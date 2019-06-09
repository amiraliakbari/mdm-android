package com.sabaos.saba.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.sabaos.saba.Utils.RegisterApp.marketTimer;
import static com.sabaos.saba.Utils.RegisterApp.riotTimer;
import static com.sabaos.saba.service.WebSocketService.sharedPref;
import static com.sabaos.saba.service.WebSocketService.ws;


public class MessageHandle {

    public void handleReceivedMessages(Context context, String text) {

        try {

            JSONObject jsonObject = new JSONObject(text);
            String type = jsonObject.getString("type");
            switch (type) {

                case "register":
                    if (jsonObject.getString("result").equalsIgnoreCase("success")) {

                        Log.i("client registration", "success");
                    } else Log.i("client registration", "failure");

                    break;
                case "registerApp":
                    if (jsonObject.getString("app").equalsIgnoreCase("com.sabaos.testmarketapp")){

                        if (jsonObject.getString("result").equalsIgnoreCase("success")) {

                            Log.i("app registration", "success");
                            sharedPref.saveData("IsMarketRegistered", "true");
                            marketTimer.cancel();
                            marketTimer.purge();
                        } Log.i("app registration", "failure");


                    }else if (jsonObject.getString("app").equalsIgnoreCase("com.sabaos.testriotapp")){

                        if (jsonObject.getString("result").equalsIgnoreCase("success")) {

                            Log.i("app registration", "success");
                            sharedPref.saveData("IsRiotRegistered", "true");
                            riotTimer.cancel();
                            riotTimer.purge();
                        } Log.i("app registration", "failure");

                    }
                    break;
                case "push":

                    String app = jsonObject.getString("app");
                    if (app.equalsIgnoreCase("com.sabaos.testmarketapp")) {
                        Intent intent = new Intent();
                        intent.putExtra("type", "push");
                        intent.putExtra("data", jsonObject.getString("data"));
                        intent.setComponent(new ComponentName("com.sabaos.testmarketapp", "com.sabaos.testmarketapp.MyIntentService"));
                        if (Build.VERSION.SDK_INT >= 26) {
                            context.startForegroundService(intent);
                        } else {
                            context.startService(intent);
                        }

                        //send ack back to server
                        String ack = "{" +
                                "\"type\":\"push\"," +
                                "\"token\":\"" + new SharedPref(context).loadData("marketToken") +
                                "\",\"data\":\""+ "first test push message" +"\"" +
                                "\"result\":\"success\"" +
                                "}";
                        ws.send(ack);

                    } else if (app.equalsIgnoreCase("com.sabaos.testriotapp")) {

                        Intent intent = new Intent();
                        intent.putExtra("type", "push");
                        intent.putExtra("data", jsonObject.getString("data"));
                        intent.setComponent(new ComponentName("com.sabaos.testriotapp", "com.sabaos.testriotapp.MyIntentService"));
                        if (Build.VERSION.SDK_INT >= 26) {
                            context.startForegroundService(intent);
                        } else {
                            context.startService(intent);
                        }

                        //send ack back to server
                        String ack = "{" +
                                "\"type\":\"push\"," +
                                "\"token\":\"" + new SharedPref(context).loadData("riotToken") +
                                "\",\"data\":\""+ "first test push message" +"\"" +
                                "\"result\":\"success\"" +
                                "}";
                        ws.send(ack);

                    } else
                        Log.i("push received", "invalid app name");
                    break;
            }

        } catch (
                JSONException e) {
            e.printStackTrace();
        }

    }
}
