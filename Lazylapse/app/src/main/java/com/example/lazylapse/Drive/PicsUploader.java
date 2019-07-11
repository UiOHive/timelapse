package com.example.lazylapse.Drive;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.lazylapse.App;
import com.example.lazylapse.Interface.Controller;
import com.example.lazylapse.Interface.Logger;
import com.example.lazylapse.Photo.LogPictures;

import java.io.File;
import java.util.ArrayList;

public class PicsUploader extends Service {
    public PicsUploader() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = intent.getStringExtra(Controller.ACCESS_EXTRA);
        Logger logger = Logger.getLogger();
        logger.addToLog(accessToken);

        if(accessToken != null) { //check if we're authorized and identified to upload

            String lastPicture = prefs.getString("lastPictureUploaded", "none");

            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "LazyLapse");

            ArrayList<String> listOfFiles = LogPictures.getFilesToUpload();

            if(listOfFiles == null){
                Logger.getLogger().addToLog("uploading: "+listOfFiles);
                return flags;
            }

            try {
                for (String path : listOfFiles) {
                    path = path.replace("\n", "");
                    Logger.getLogger().addToLog("uploading: " + path);
                    File file = new File(path);
                    if (file.isFile()) {
                        try {
                            new UploadTask(DropboxClient.getClient(accessToken), file, getApplicationContext()).execute();
                        } catch (Exception e) {
                            Toast.makeText(this, "b " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
                LogPictures.clear();
            }catch (Exception e){
                Logger.getLogger().addToLog(e.getMessage());
            }
        }}
        catch(Exception e){
            Toast.makeText(this,"a "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return flags;
    }
}
