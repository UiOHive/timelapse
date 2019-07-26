package com.example.lazylapse.Drive;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.lazylapse.Interface.Controller;
import com.example.lazylapse.Interface.Logger;
import com.example.lazylapse.Photo.LogPictures;

import java.io.File;

public class LogUploader extends Service {
    public LogUploader() {
    }

    /**
     * LogUploader is not supposed to be bound, this method isn't implemented and will not be in
     * the foreseeable future.
     * @param intent
     * @return nothing
     */
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Service PicsUploader not supposed to be bound");
    }

    /**
     * This is the method that will be called when we start the service using an intent of this
     * class with startService(intent). The parameters will be filled automatically with
     * corresponding data. The intent should contain an Extra with {@link Controller#ACCESS_EXTRA}
     * as key. Once started it will get a list of files from {@link LogPictures} and then use the
     * access token to obtain a dropbox client, that, in turns, will be used to upload the file to
     * dropbox with {@link UploadTask}.
     *
     * @param intent the intent used to start the service (need to contain an Extra {@link Controller#ACCESS_EXTRA}
     * @param flags
     * @param startId
     *
     * @return the same flag that it had as parameter
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Logger logger = Logger.getLogger();

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String accessToken = intent.getStringExtra(Controller.ACCESS_EXTRA);


            if (accessToken != null) { //check if we're authorized and identified to upload

                String lastPicture = prefs.getString("lastPictureUploaded", "none");

                File logFile = new File(Logger.getDir(), logger.getPathToLog());
                if (logFile.exists()) {
                    try {
                        new UploadTask(DropboxClient.getClient(accessToken), logFile, getApplicationContext()).execute();
                    } catch (Exception e) {
                        logger.appendLog(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.appendLog(e.getMessage());
        }

        return flags;
    }
}
