package com.example.zaverecka;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

// trida pro planovani a ruseni notifikaci
public class UpozorneniHelper {

    // naplanovani notifikace na kazdy den
    public static void scheduleDailyNotification(Context context, int hour, int minute) {
        // sys sluzba alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // vytvoreni intentu pro upozorneni - classu
        Intent intent = new Intent(context, Upozorneni.class);

        // pending intent bude pouzitej pri alarmu
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // vytvoreni kalendare s aktualnim casem - picker na cas
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // nastaveni casu pro notifikaci od usera
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // pokud zvolime cas v minulosti notifikace se nastavi na dalsi den
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // opakovani notifikace na kazdy den
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,               // Probudí zařízení při vypnuté obrazovce
                calendar.getTimeInMillis(),            // Čas první notifikace
                AlarmManager.INTERVAL_DAY,             // Opakuje se každý den
                pendingIntent                          // Co se má spustit
        );
    }

    // zruseni naplanovane notifikace
    public static void cancelNotification(Context context) {
        // sys sluzba alarm manager vol. 2
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // vytvorení stejneho intentu jako pri planovani (musi byt stejny jinak ho nejde)
        Intent intent = new Intent(context, Upozorneni.class);

        // pending intent bude pouzitej pri alarmu - stejny jako vys
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // vypnuti notifikace
        alarmManager.cancel(pendingIntent);
    }
}
