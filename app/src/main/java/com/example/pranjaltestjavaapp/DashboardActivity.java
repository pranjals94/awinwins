package com.example.pranjaltestjavaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {
    Button btnStartMonitoring,btnStopMonitoring;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        intent = new Intent(DashboardActivity.this, MyService.class);
        Intent dashboard = getIntent();
        String token = dashboard.getStringExtra(MainActivity.EXTRA_NAME);

        btnStartMonitoring = findViewById(R.id.startMonitoring);
        btnStopMonitoring = findViewById(R.id.stopMonitoring);

        btnStartMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyServiceRunning()) {
                    intent.putExtra("token", token);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    }else{startService(intent);}
                    makeToast("Service Started");
                }else{makeToast("Service Already Running");}
            }
        });

        btnStopMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
                makeToast("Service Stopped !");
            }
        });
    }
    //-----------------onCreate Ends --------------------------------------------
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }
//testing -------------github change
    Toast t;
    public void makeToast(String msg){
        if (t!=null)
            t.cancel();
        t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        View v1 = t.getView();
        v1.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
        t.setGravity(Gravity.TOP,0,0);
        t.show();
    }

    @Override
    public void onBackPressed(){
        makeToast("Cant' Go back Already Logged In !");
    }


    public boolean isMyServiceRunning(){
        return isServiceRunning(getApplicationContext(), MyService.class);
    }

    public boolean isServiceRunning(Context c, Class<?> serviceClass){
        ActivityManager activityManager = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo runningServiceInfo: services){
            if(runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }

}