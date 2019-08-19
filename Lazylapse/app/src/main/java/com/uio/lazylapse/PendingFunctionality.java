package com.uio.lazylapse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public interface PendingFunctionality {

    public default void repeat(PendingIntent pendingIntent, String intervalKey){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        int interval = Integer.valueOf(prefs.getString(intervalKey, "30")); //the interval between pictures in minutes

        ((AlarmManager)App.getContext().getSystemService(Context.ALARM_SERVICE)).setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+interval * 60 * 1000,pendingIntent);
    }
}
