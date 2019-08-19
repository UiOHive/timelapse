package com.uio.lazylapse.Interface;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.v2.users.FullAccount;
import com.uio.lazylapse.App;
import com.uio.lazylapse.Constant;
import com.uio.lazylapse.Drive.DropboxClient;
import com.uio.lazylapse.Drive.LogUploader;
import com.uio.lazylapse.Drive.LoginActivityDropbox;
import com.uio.lazylapse.Drive.PicsUploader;
import com.uio.lazylapse.SMS.PhoneStatus;
import com.uio.lazylapse.Photo.Photographer;
import com.uio.lazylapse.R;
import com.uio.lazylapse.SMS.SMSManager;
import com.uio.lazylapse.Drive.URI_To_Path;
import com.uio.lazylapse.Drive.UploadTask;
import com.uio.lazylapse.Drive.UserAccountTask;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

/**
 * Activity launched when the app start, it allows the user to launch the timelapse and to set
 * parameters. It also display a log that keep track of every action taken by the app, the same that
 * will be uploaded on dropbox.
 */
public class Controller extends AppCompatActivity implements ILogVisitor {
    private static final int REQUEST_CODE_SIGN_IN = 2 ;
    public static final String ACCESS_EXTRA = "accessExtra";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 43;
    private static final int REQUEST_SEND_SMS = 44 ;
    private ImageButton settingsButton;
    private ImageButton cameraButton;
    private Photographer photographer;
    private View timeLapseButton;
    private AlarmManager alarmMgr;
    private TextView textLog;
    private Logger logger;
    private SMSManager smsManager;
    private SharedPreferences prefs;

    private static final int IMAGE_REQUEST_CODE = 101;
    private String ACCESS_TOKEN;
    private int REQUEST_CAMERA = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        photographer = new Photographer();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.buttonSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Controller.this, SettingsActivity.class);
                startActivity(i);
            }
        });
        cameraButton = findViewById(R.id.buttonTimeLapse);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentDropbox = new Intent(Controller.this, LogUploader.class);
                intentDropbox.putExtra(ACCESS_EXTRA,retrieveAccessToken());
                startService(intentDropbox);
                /*Intent i = new Intent("com.airplanemode.ON");
                sendBroadcast(i);*/

            }
        });
        timeLapseButton = findViewById(R.id.buttonLaunchTimeLapse);
        timeLapseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intentCamera = new Intent(Controller.this, Photographer.class);
                    intentCamera.putExtra(Constant.INSTANT_PICTURE, true);
                    intentCamera.putExtra("Activity",true);

                    Intent intentDropbox = new Intent(Controller.this, PicsUploader.class);
                    intentDropbox.putExtra(ACCESS_EXTRA,retrieveAccessToken());

                    Intent intentLogDropbox = new Intent(Controller.this, LogUploader.class);
                    intentLogDropbox.putExtra(ACCESS_EXTRA,retrieveAccessToken());

                    Intent intentPhoneStatus = new Intent(Controller.this, PhoneStatus.class);

                    PendingIntent previousIntentCamera = PendingIntent.getActivity(Controller.this, 1, intentCamera, PendingIntent.FLAG_NO_CREATE);
                    PendingIntent previousIntentDropbox = PendingIntent.getService(Controller.this, 1, intentDropbox, PendingIntent.FLAG_NO_CREATE);
                    PendingIntent previousIntentLogDropbox = PendingIntent.getService(Controller.this, 1, intentLogDropbox, PendingIntent.FLAG_NO_CREATE);
                    PendingIntent previousIntentPhoneStatus = PendingIntent.getService(Controller.this, 1, intentPhoneStatus, PendingIntent.FLAG_NO_CREATE);

                    if (null != previousIntentCamera) {
                        alarmMgr.cancel(previousIntentCamera);
                        previousIntentCamera.cancel();

                        alarmMgr.cancel(previousIntentDropbox);
                        previousIntentDropbox.cancel();

                        alarmMgr.cancel(previousIntentLogDropbox);
                        previousIntentLogDropbox.cancel();

                        alarmMgr.cancel(previousIntentPhoneStatus);
                        previousIntentPhoneStatus.cancel();

                        logger.appendLog("Time lapse ended");
                    } else {

                        setUpAlarmIntent("pictureInterval",0, intentCamera);
                        setUpAlarmIntent("dropboxInterval",1*60*1000, intentDropbox);
                        setUpAlarmIntent("phoneStatusInterval",0, intentPhoneStatus);
                        setUpAlarmIntent("LogUploaderInterval",0, intentLogDropbox);
                        String nameOfPhone = prefs.getString("nameOfPhone", "'name could not be fetched'");

                        smsManager.sendMessage("Time Lapse Started on phone " + nameOfPhone);

                        logger.appendLog("Time lapse started");
                    }
                } catch (Exception e) {
                    Toast.makeText(App.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        textLog = (TextView) findViewById(R.id.textView);
        textLog.setMovementMethod(new ScrollingMovementMethod());

        smsManager = SMSManager.getSMSManager();

        try {
            logger = Logger.getLogger();
            logger.accept(this);
            textLog.setText(logger.getLog());
        } catch (Exception e) {
            textLog.setText(e.getMessage());
        }
        if (!tokenExists()) {
            //No token
            //Back to LoginActivity
            Intent intent = new Intent(Controller.this, LoginActivityDropbox.class);
            startActivity(intent);
        }

        ACCESS_TOKEN = retrieveAccessToken();

        askPermission(Manifest.permission.CAMERA,REQUEST_CAMERA,R.string.permission_camera_rationale);
        askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,REQUEST_WRITE_EXTERNAL_STORAGE,R.string.permission_storage_rationale);
        askPermission(Manifest.permission.SEND_SMS,REQUEST_SEND_SMS,R.string.permission_sms_rationale);

        getUserAccount();
    }

    /**
     * this is called by Logger whenever a new line is append to the log .
     * @param log String, the whole log (will be reset on a monthly basis)
     */
    @Override
    public void visit(String log) {
        textLog.setText(log);
    }

    /**
     * add to {@link Logger } the authenticated account details retrieved with {@link UserAccountTask}
     */
    protected void getUserAccount() {
        if (ACCESS_TOKEN == null)return;
        new UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                //Print account's info
                Log.d("User", account.getEmail());
                Log.d("User", account.getName().getDisplayName());
                Log.d("User", account.getAccountType().name());
                logger.appendLog(account.getName().getDisplayName()+" "+account.getEmail());
            }
            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account details.");
            }
        }).execute();
    }

    /**
     * check if the user is authenticated on dropbox
     * @return boolean, true if authenticated, false if not.
     */
    private boolean tokenExists() {
        SharedPreferences prefs = getSharedPreferences("com.example.valdio.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

    /**
     * Retrieve the access token from the shared preferences
     * @return String, Access token
     */
    public String retrieveAccessToken() {
        //check if ACCESS_TOKEN is stored on previous app launches
        SharedPreferences prefs = getSharedPreferences("com.example.valdio.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found");
            return null;
        } else {
            //accessToken already exists
            Log.d("AccessToken Status", "Token exists");
            return accessToken;
        }
    }
    private void upload() {
        if (ACCESS_TOKEN == null)return;
        //Select image to upload
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,
                "Upload to Dropbox"), IMAGE_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        // Check which request we're responding to
        if (requestCode == IMAGE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //Image URI received
                File file = new File(URI_To_Path.getPath(getApplication(), data.getData()));
                if (file != null) {
                    //Initialize UploadTask
                    new UploadTask(DropboxClient.getClient(ACCESS_TOKEN), file, getApplicationContext()).execute();
                }
            }
        }
    }

    /**
     * Used to set action to be executed at given interval (retrieved from user preference)
     * @param intervalKey String key to the interval stored in shared preference (user preference)
     * @param intent the intentent coresponding to the activity to launch
     */
    public void setUpAlarmIntent(String intervalKey, int delay, Intent intent){

        int interval = Integer.valueOf(prefs.getString(intervalKey, "30")); //the interval between pictures in minutes

        PendingIntent pendingIntent = null;
        if(intent.getBooleanExtra("Activity", false)) {
            pendingIntent = PendingIntent.getActivity(Controller.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }else{
            pendingIntent = PendingIntent.getService(Controller.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmMgr.cancel(pendingIntent);
        /*alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+(delay+interval) * 60 * 1000,pendingIntent);*/

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay,
                interval * 60 * 1000, pendingIntent);

        // we do not use setRepeating as it seems it doesn't get the phone out of sleep mode,
        // which mess with network among other thing
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.load();
        logger.updateVisitors();
    }

    /**
     * ask permissions to user with popup at run time, if denied, explains why permission is
     * required and then ask once again.
     * @param permission permission asked to user
     * @param requestCode request code for eventual post treatment of the result
     * @param explanations explaination of why the permission is needed
     */
    private void askPermission(String permission, int requestCode, int explanations){
        View mLayout = findViewById(R.id.layoutMain);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mLayout, explanations,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(Controller.this,
                                        new String[]{permission},
                                        requestCode);
                            }
                        })
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        requestCode);


            }
        } else {
            // Permission has already been granted
        }
    }


}
