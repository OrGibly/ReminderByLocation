package com.example.reminderbylocation.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.reminderbylocation.Data.AlertData;
import com.example.locationbyreminder.R;

public class AlertNotification extends Notification {

    private static final String CHANNEL_ID = "channel1";
    private Context context;

    private final String title;
    private final String text;
    private final int notificationId;

    public AlertNotification(Context context, AlertData alertData){
        this.context = context;
        this.title = alertData.getName();
        this.text = alertData.getDetails();
        this.notificationId = alertData.getId();
        createNotificationChannel();
    }

    public void notifyUser(){
        NotificationManagerCompat.from(context).notify(notificationId, createBuilder().build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Main";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder createBuilder(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(this.title)
                .setContentText(this.text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);

        return builder;
    }
}
