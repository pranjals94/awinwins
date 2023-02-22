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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    Button  btnLogIn;
    EditText editTextPhoneNo, editTextPassword;
    public static final String EXTRA_NAME = "token";
    private final OkHttpClient client = new OkHttpClient();
    Vibrator shake;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main); // call this on top else cant find the elements (findviewbyid)
        super.onCreate(savedInstanceState);
        Gson gson =new Gson();
        shake = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        btnLogIn= findViewById(R.id.logInButton);


        editTextPhoneNo= findViewById(R.id.phoneNo);
        editTextPassword= findViewById(R.id.password);



        OkHttpClient client1 = client.newBuilder()
                .readTimeout(2000, TimeUnit.MILLISECONDS)
                .connectTimeout(2000, TimeUnit.MILLISECONDS)
                .build();
         String url = "https://art.winwins.app/api/user/login";


        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody formBody = new FormBody.Builder()
                        .add("account",  "+91".concat(editTextPhoneNo.getText().toString()))
                        .add("password", editTextPassword.getText().toString())
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();
                client1.newCall(request).enqueue(new Callback() { //enqueue makes to run on a different thread
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        System.out.println("error");
                        MainActivity.this.runOnUiThread(new Runnable() { //update ui by running on ui thread
                            @Override
                            public void run() {
                                makeToast("Something Went Wrong");
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.isSuccessful()){
                            String responseBody = response.body().string();
//                            System.out.println(responseBody);
//                           test data = gson.fromJson("{\"body\":\"John\", \"age\":30, \"car\":null}", test.class);
                            logInUserSchema logInData = gson.fromJson(responseBody, logInUserSchema.class);
                            if(Objects.equals(logInData.code, "1")){
                                //----------------vibrate--------------------------
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    shake.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    shake.vibrate(100);
                                }

                                //---------------------vibrate code ends------------------
//                            System.out.println(logInData.time);
//                            System.out.println(logInData.data.userinfo.token);
                            MainActivity.this.runOnUiThread(new Runnable() { //update ui by running on ui thread
                                @Override
                                public void run() {
                                    Intent dashboard;
                                    dashboard = new Intent(MainActivity.this, DashboardActivity.class);
                                    dashboard.putExtra(EXTRA_NAME,logInData.data.userinfo.token);
                                    startActivity(dashboard);
                                }
                            });}else{
                                MainActivity.this.runOnUiThread(new Runnable() { //update ui by running on ui thread
                                    @Override
                                    public void run() {
                                        makeToast("User Not Found !");
                                    }
                                });

                            }
                        }
                    }
                });

            }
        });
    }
   //----------------------------onCreate Ends -------------------------------------------------------------


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

    public boolean isMyServiceRunning(){
        return isServiceRunning(getApplicationContext(), MyService.class);
    }

    public boolean isServiceRunning(Context c,Class<?> serviceClass){
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