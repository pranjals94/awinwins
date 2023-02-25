package com.example.pranjaltestjavaapp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

public class MyService extends Service {
    MediaPlayer mp;
    Vibrator shake;
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayerResources();
        }
    };

    String token;
    private final Handler mHandler = new Handler();
    private static final String CHANNEL_ID = "My Channel";

    private final OkHttpClient client = new OkHttpClient();
    Gson gson =new Gson();

    OkHttpClient client1 = client.newBuilder()
            .readTimeout(2000, TimeUnit.MILLISECONDS)
            .connectTimeout(2000, TimeUnit.MILLISECONDS)
            .build();
    String url = "https://art.winwins.app/api/project/game_history";
    RequestBody formBody = new FormBody.Builder()
            .add("project_id", "3")
            .add("page", "1")
            .add("limit", "10")
            .build();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        token = intent.getStringExtra("token");
        shake = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Intent intent1 = new Intent(this, DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent1,0);
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID).
                setSmallIcon(R.drawable.ic_baseline_miscellaneous_services_24)
                .setContentTitle("Awinwins service running in background")
                .setContentText("Mendetory notification for Forground services. Android 12(API-31) and Later.")
//                .setContentIntent(pendingIntent) //makes opening the app possible on clicking the notification.(app reopens with initial states)
                .build();
        startForeground(1,notification);


        getGameHistory.run();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(getGameHistory);
        stopForeground(true);
        stopSelf();
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
    private final Runnable getGameHistory = new Runnable() {
        @Override
        public void run() {
                Request request = new Request.Builder()
                        .addHeader("token", token)
                        .url(url)
                        .post(formBody)
                        .build();

                client1.newCall(request).enqueue(new Callback() { //enqueue makes to run on a different thread
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                    e.printStackTrace();
//                    System.out.println("error");
                        ShowNotification("HTTP Failure !", "Check internet Connectivity !", R.drawable.ic_baseline_add_alert_24);
//                    stopSelf();
                        mHandler.postDelayed(getGameHistory, 50000); //keep looping network may come
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) { // triggers only if status code is less than 300
                            String responseBody = response.body().string();
//                            System.out.println(responseBody);
//                           test data = gson.fromJson("{\"body\":\"John\", \"age\":30, \"car\":null}", test.class);
                            gameHistorySchema gameHistorydata = gson.fromJson(responseBody, gameHistorySchema.class);
                            List<listMap> gameData = gameHistorydata.data.list;
                            int red = 0, green = 0;
                            for (int i = gameData.size() - 1; i > 0; i--) {
                                String[] arrOfStr = gameData.get(i).color.split(",", 2);
//                                System.out.println(gameData.get(i).color);
                                System.out.println(arrOfStr[0]);
                                System.out.println(gameData.get(i).sn);
                                if (Objects.equals(arrOfStr[0], "red")) {
                                    red++;
                                    green = 0;
                                }
                                if (Objects.equals(arrOfStr[0], "green")) {
                                    green++;
                                    red = 0;
                                }
                            }
//                        System.out.println(red);
//                        System.out.println(green);
                            if (red > 5) {
                                ShowNotification("Color Is Red !", "Red greater than 5", R.drawable.ic_baseline_attach_money_24);
                            }
                            if (green > 5) {
                                ShowNotification("Color Is green !", "Green greater than 5", R.drawable.ic_baseline_attach_money_24);
                            }
                            mHandler.postDelayed(getGameHistory, 50000);//trigger after 50 Secs
                        } else {
                            System.out.println("Error in authentication");
                            ShowNotification("Not Authenticated !", "Please Close the App and LogIn Again !", R.drawable.ic_baseline_add_alert_24);
                            stopForeground(true);
                            stopSelf(); //self stop service
                        }
                    }
                });
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ChannelNamePranjal";
            String description = "ChannelDescription:Hello";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);


            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void ShowNotification(String title, String description, int icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(description)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(6, builder.build());

        //-------------sound notification-------------
        mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
        mp.start();
        mp.setOnCompletionListener(onCompletionListener);
        //-------------Vibrate-------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shake.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            shake.vibrate(300);
        }


    }

    // dedicated function is made to check the
    // mediaPlayer instance is null or not
    // based on that the actions are taken on
    // the mediaPlayer instance
    void releaseMediaPlayerResources() {
        if (mp != null) {

            // it is safe to stop playing the audio
            // file before releasing the audio file
            mp.stop();
            mp.reset();

            // after stop playing the audio file
            // release the audio resources
            mp.release();
        }
    }

}
