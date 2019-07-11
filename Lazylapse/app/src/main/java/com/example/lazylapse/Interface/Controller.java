package com.example.lazylapse.Interface;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.v2.users.FullAccount;
import com.example.lazylapse.App;
import com.example.lazylapse.Constant;
import com.example.lazylapse.Drive.DropboxClient;
import com.example.lazylapse.Drive.LoginActivityDropbox;
import com.example.lazylapse.Drive.NewPicsUploader;
import com.example.lazylapse.Drive.PicsUploader;
import com.example.lazylapse.SMS.PhoneStatus;
import com.example.lazylapse.Photo.Photographer;
import com.example.lazylapse.R;
import com.example.lazylapse.SMS.SMSManager;
import com.example.lazylapse.Drive.URI_To_Path;
import com.example.lazylapse.Drive.UploadTask;
import com.example.lazylapse.Drive.UserAccountTask;

import java.io.File;


public class Controller extends AppCompatActivity implements ILogVisitor {
    private static final int REQUEST_CODE_SIGN_IN = 2 ;
    public static final String ACCESS_EXTRA = "accessExtra";
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
                Intent i = new Intent(Controller.this, PicsUploader.class);
                i.putExtra(ACCESS_EXTRA,retrieveAccessToken());
                startService(i);
            }
        });
        timeLapseButton = findViewById(R.id.buttonLaunchTimeLapse);
        timeLapseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intentCamera = new Intent(Controller.this, Photographer.class);
                    intentCamera.putExtra(Constant.INSTANT_PICTURE, true);

                    Intent intentDropbox = new Intent(Controller.this, NewPicsUploader.class);

                    Intent intentPhoneStatus = new Intent(Controller.this, PhoneStatus.class);

                    PendingIntent previousIntentCamera = PendingIntent.getActivity(Controller.this, 1, intentCamera, PendingIntent.FLAG_NO_CREATE);
                    PendingIntent previousIntentDropbox = PendingIntent.getActivity(Controller.this, 1, intentDropbox, PendingIntent.FLAG_NO_CREATE);
                    PendingIntent previousIntentPhoneStatus = PendingIntent.getActivity(Controller.this, 1, intentPhoneStatus, PendingIntent.FLAG_NO_CREATE);

                    if (null != previousIntentCamera) {
                        alarmMgr.cancel(previousIntentCamera);
                        previousIntentCamera.cancel();

                        alarmMgr.cancel(previousIntentDropbox);
                        previousIntentDropbox.cancel();

                        alarmMgr.cancel(previousIntentPhoneStatus);
                        previousIntentPhoneStatus.cancel();

                        logger.addToLog("Time lapse ended");
                    } else {

                        setUpAlarmIntent("pictureInterval", intentCamera);
                        setUpAlarmIntent("dropboxInterval", intentDropbox);
                        setUpAlarmIntent("phoneStatusInterval", intentPhoneStatus);
                        String nameOfPhone = prefs.getString("nameOfPhone", "'name could not be fetched'");

                        smsManager.sendMessage("Time Lapse Started on phone " + nameOfPhone);




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
        getUserAccount();
    }

    @Override
    public void visit(String log) {
        textLog.setText(log);
    }

    protected void getUserAccount() {
        if (ACCESS_TOKEN == null)return;
        new UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                //Print account's info
                Log.d("User", account.getEmail());
                Log.d("User", account.getName().getDisplayName());
                Log.d("User", account.getAccountType().name());
                logger.addToLog(account.getName().getDisplayName()+" "+account.getEmail());
            }
            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account details.");
            }
        }).execute();
    }
    private boolean tokenExists() {
        SharedPreferences prefs = getSharedPreferences("com.example.valdio.dropboxintegration", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

    private String retrieveAccessToken() {
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
    private void setUpAlarmIntent(String intervalKey, Intent intent){

        int interval = Integer.valueOf(prefs.getString(intervalKey, "30")); //the interval between pictures in minutes

        PendingIntent pendingIntent = PendingIntent.getActivity(Controller.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.cancel(pendingIntent);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval * 1000,
                interval * 60 * 1000, pendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.load();
        logger.updateVisitors();
    }


}
