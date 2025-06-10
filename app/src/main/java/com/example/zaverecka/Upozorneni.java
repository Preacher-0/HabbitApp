package com.example.zaverecka;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Notification;
import android.os.Build;

import androidx.core.app.NotificationCompat;

// BroadcastReceiver prijme udalost z alarm manageru na notif.
public class Upozorneni extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // co se ma vypsat v notifikaci
        String title = "Připomínka!";
        String message = "Nezapomeň trénovat svůj návyk.";

        // Získání správce notifikací ze systému
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "daily_channel"; // ID notifikačního kanálu

        // vytvoreni notifikacniho kanalu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Denní připomínky",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // sestaveni notifikace
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)                     // Titulek notifikace
                .setContentText(message)                   // Text notifikace
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Ikonka
                .setAutoCancel(true)                       // Zavřít po kliknutí
                .build();

        // zobrazení notifikace
        notificationManager.notify(1, notification);
    }
}
