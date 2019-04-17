package com.example.sabacloudmessaging;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    SharedPreferences FCMPref;
    public SharedPref(Context context){

        FCMPref = context.getSharedPreferences("FCMPref", context.MODE_PRIVATE);
    }

    public void saveData(String key, String value){

        SharedPreferences.Editor editor = FCMPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String loadData(String key){

        String value = FCMPref.getString(key, "No such entry as " + key );
        return value;
    }

}
