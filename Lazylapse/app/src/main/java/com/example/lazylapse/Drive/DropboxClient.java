package com.example.lazylapse.Drive;

import android.content.Intent;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Class used to obtain DbxClientV2 to a client needed to upload data to dropbox with {@link UploadTask}
 *
 * authored by Valdio Veliu on https://www.sitepoint.com/adding-the-dropbox-api-to-an-android-app/
 */
public class DropboxClient {
    /**
     * Return the client used by {@link UploadTask} which is initiated in {@link PicsUploader#onStartCommand(Intent, int, int)}
     * @param ACCESS_TOKEN String, access token obtained in {@link LoginActivityDropbox} and stored
     *                     in the default sharedPreference
     * @return DbxClientV2, the client used by {@link UploadTask}
     */
    public static DbxClientV2 getClient(String ACCESS_TOKEN) {
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        return client;
    }
}
