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

import java.io.File;

public class NewPicsUploader extends Service {
    public NewPicsUploader() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken = intent.getStringExtra(Controller.ACCESS_EXTRA);
        Logger logger = Logger.getLogger();
        logger.addToLog(accessToken);
        if(accessToken != null) { //check if we're authorized and identified to upload

            String lastPicture = prefs.getString("lastPictureUploaded", "none");

            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "LazyLapse");
            File[] listOfFiles = folder.listFiles();

            String nameLastUploadedFileInSession ="none";

            if (lastPicture.equals("none")) {
                logger.addToLog("first upload to Dropbox");
                for(File file: listOfFiles){
                    if(file.isFile()){
                        try {
                            new UploadTask(DropboxClient.getClient(accessToken), file, getApplicationContext()).execute();
                            nameLastUploadedFileInSession = file.getName();
                        }catch(Exception e){
                            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }else{
                logger.addToLog("Upload to Dropbox");
                for(File file: listOfFiles){
                    if(file.isFile()){
                        if(file.getName().compareTo(lastPicture)<0) { //compares Lexicographically as images are named using dates
                            try {
                                new UploadTask(DropboxClient.getClient(accessToken), file, getApplicationContext()).execute();
                                nameLastUploadedFileInSession = file.getName();
                            }catch(Exception e){
                                Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
            prefs.edit().putString("lastPictureUploaded", nameLastUploadedFileInSession).commit();
        }
        return flags;
    }
}
