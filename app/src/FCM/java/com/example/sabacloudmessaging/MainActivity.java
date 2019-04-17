package com.example.sabacloudmessaging;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    SharedPref FCMSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FCMSharedPref = new SharedPref(this);
        Log.d("Firebase", "token " + FCMSharedPref.loadData("token"));
        TextView Token = (TextView) findViewById(R.id.welcome);
        Token.setText(getString(R.string.welcome) + " Your token is: " + FCMSharedPref.loadData("token"));
    }
}