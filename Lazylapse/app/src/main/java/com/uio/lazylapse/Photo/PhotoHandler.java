package com.uio.lazylapse.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.BatteryManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.uio.lazylapse.App;

/**
 * Handles picture saving after they are taken (see {@link Photographer}).
 */
public class PhotoHandler implements PictureCallback {

    private final Context context;

    public PhotoHandler(Context context) {
        this.context = context;
    }

    /**
     * Called when the pictures is taken in {@link Photographer}, it handles saving the picture file
     * naming it based on the time of capture and add it to two logs {@link Logger} to display the
     * succes on the log and {@link LogPictures} to put it on the list of file to upload next time
     * we send pictures to dropbox with {@link PicsUploader}.
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(Photographer.DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        String date = getDateFormatted();



        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String nameOfPhone = prefs.getString("nameOfPhone", "'name could not be fetched'");

        String BatteryLevel = "B"+String.valueOf(getBatPercentage());
        String photoFile = date+ "_" +nameOfPhone+ "_" + BatteryLevel + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            (new LogPictures()).appendLog(filename);
            //Toast.makeText(context, "New Image saved:" + filename,
             //       Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(Photographer.DEBUG_TAG, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get the directory in which the picture will be saved (local storage -> pictures -> LazyLapse)
     * @return File directory in which the picture will be saved
     */
    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Lazylapse");
    }

    /**
     * get battery in percent to be added to the file name
     * @return float battery percentage
     */
    private int getBatPercentage(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = App.getContext().registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) (level / (float)scale *100);
        return batteryPct;
    }

    /**
     * get the formatted date that is used in order to name file
     * @return String formatted date
     */
    private String getDateFormatted(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'__'HHmmss");
        String date = dateFormat.format(new Date());
        return date;
    }
}