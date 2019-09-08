package com.uio.lazylapse.Drive;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.uio.lazylapse.Interface.Controller;
import com.uio.lazylapse.Interface.Logger;
import com.uio.lazylapse.PendingFunctionality;
import com.uio.lazylapse.Photo.LogPictures;

import java.io.File;

public class PicsUploader extends Service implements PendingFunctionality {
    public PicsUploader() {
    }

    /**
     * PicsUploader is not supposed to be bound, this method isn't implemented and will not be in
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

        try{

        String accessToken = intent.getStringExtra(Controller.ACCESS_EXTRA);

        /*Intent i = new Intent("com.airplanemode.OFF");
        sendBroadcast(i);*/



        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Logger logger = Logger.getLogger();

        if(accessToken != null) { //check if we're authorized and identified to upload

            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "LazyLapse");

            String[] listOfFiles = (new LogPictures()).getFilesToUpload();
            LogPictures.clear();
            if(listOfFiles == null){
                Logger.getLogger().appendLog("uploading null list of files");
                return flags;
            }

            try {
                for (String path : listOfFiles) {
                    path = path.replace("\n", "");
                    Logger.getLogger().appendLog("uploading: " + path);
                    File file = new File(path);
                    if (file.isFile()) {
                        try {
                            UploadTask upload = new UploadTask(DropboxClient.getClient(accessToken), file, getApplicationContext());
                            upload.execute();
                            logger.appendLog(upload.reponse.toString());
                        } catch (Exception e) {
                            Toast.makeText(this, "b " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }catch(Exception e){
                Logger.getLogger().appendLog(e.getMessage());
            }
        }
        /*i = new Intent("com.airplanemode.ON");
        sendBroadcast(i);*/
        }
        catch(Exception e){
            Toast.makeText(this,"a "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return flags;
    }
}
