package com.example.noteapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyAlertBroadcast extends BroadcastReceiver {
    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        String EVENT = bundle.getString("EVENT","No Event");
        String TIME = bundle.getString("TIME","00:00");
        int ID = bundle.getInt("ID",0);
        //Toast.makeText(context,String.valueOf(ID),Toast.LENGTH_LONG).show();

        Intent intent1 = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,ID,intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        String channelId = "Android_20_3";
        CharSequence channelName = "Notification System";
        String channelDescription = "Alarm Not To Dismiss Event";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification notification = new NotificationCompat.Builder(context,channelId)
                .setContentTitle(TIME)
                .setContentText(EVENT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setChannelId(channelId)
                .setGroup("calendar_view_group")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(ID,notification);
    }
}
