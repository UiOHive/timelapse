package com.example.lazylapse.Drive;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Use AsyncTask to upload files to Dropbox. To use this class to upload new files to dropbox you
 * need first to get a dbxClient using {@link DropboxClient}. To launch this task, call the
 * constructor (which sets parameters) and then {@link #doInBackground(Object[])} will execute the
 * actual upload to dropbox.
 *
 * authored by Valdio Veliu on https://www.sitepoint.com/adding-the-dropbox-api-to-an-android-app/
 */
public class UploadTask extends AsyncTask {

    private DbxClientV2 dbxClient;
    private File file;
    private Context context;

    /**
     * The constructor will initiate the variables and then
     * @param dbxClient DbxClient, client needed for the upload obtain one with {@link DropboxClient}
     * @param file File, file we want to upload can be any file
     * @param context Context, context of our app
     */
    public UploadTask(DbxClientV2 dbxClient, File file, Context context) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.context = context;
    }

    /**
     * Open the file passed as parameter of the constructor to dropbox. This method will be executed
     * after the constructor's finished, don't call it.
     *
     * @param params The parameters of the task. The specified parameters are the parameters passed
     *               to execute(Params...) by the caller of this task.(From developer.android.com)
     * @return null (we don't need any return)
     */
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // Upload to Dropbox
            InputStream inputStream = new FileInputStream(file);
            dbxClient.files().uploadBuilder("/" + file.getName()) //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream);
            Log.d("Upload Status", "Success");
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}