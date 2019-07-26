package com.example.lazylapse;

import android.app.Application;
import android.content.Context;

import java.util.Date;

public class App extends Application {

    private static Application sApplication;

    private static boolean timeLapseStarted;

    private static Date lastSMSCheck;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    public static boolean isTimeLapseStarted() {
        return timeLapseStarted;
    }

    public static void setTimeLapseStarted(boolean started) {
        timeLapseStarted = started;
    }

    public static Date getLastSMSCheck() {
        return lastSMSCheck;
    }

    /**
     * This is not a classic setter, rather that setting the date to a given date, it uses current date
     * as value to.
     */
    public static void setLastSMSCheck() {
        lastSMSCheck = new Date();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        timeLapseStarted = false;
        lastSMSCheck = new Date();
    }

}