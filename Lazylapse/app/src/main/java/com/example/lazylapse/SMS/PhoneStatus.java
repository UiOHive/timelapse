package com.example.lazylapse.SMS;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.example.lazylapse.App;
import com.example.lazylapse.Interface.Controller;
import com.example.lazylapse.R;

import java.io.File;

public class PhoneStatus extends Service {
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.phone_status_started;
    private SMSManager smsManager;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        PhoneStatus getService() {
            return PhoneStatus.this;
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get info on the battery
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = App.getContext().registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale *100;

        smsManager = SMSManager.getSMSManager();

        String message = "";

        message += "battery level = "+batteryPct+"% \n";

        long freeBytesInternal = new File(getFilesDir().getAbsoluteFile().toString()).getFreeSpace();

        message +="Free space in internal memory = "+freeBytesInternal/8/1024+ " ko";

        String address = intent.getStringExtra("address");
        if(address!=null) {
            smsManager.sendMessage(message, address);
        }else{
            smsManager.sendMessage(message);
        }
        return flags;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Toast.makeText(this, "phone status done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Service PhoneStatus not supposed to be bound");
    }


}