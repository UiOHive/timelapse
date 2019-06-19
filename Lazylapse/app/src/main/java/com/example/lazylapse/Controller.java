package com.example.lazylapse;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class Controller extends AppCompatActivity implements ILogVisitor {
    private ImageButton settingsButton;
    private ImageButton cameraButton;
    private Photographer photographer;
    private View timeLapseButton;
    private AlarmManager alarmMgr;
    private TextView textLog;
    private Logger logger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        photographer = new Photographer();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.buttonSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Controller.this, SettingsActivity.class);
                startActivity(i);
            }});
        cameraButton = findViewById(R.id.buttonTimeLapse);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Controller.this, Photographer.class);
                i.putExtra(Constant.INSTANT_PICTURE,true);
                startActivity(i);
            }});
        timeLapseButton = findViewById(R.id.buttonLaunchTimeLapse);
        timeLapseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Intent intent = new Intent(Controller.this, Photographer.class);
                intent.putExtra(Constant.INSTANT_PICTURE,true);
                PendingIntent pendingIntent = PendingIntent.getActivity(Controller.this,1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmMgr.cancel(pendingIntent);
                alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()+60*1000,
                        30*60*1000, pendingIntent);
            }});
        textLog = (TextView) findViewById(R.id.textView);
        textLog.setMovementMethod(new ScrollingMovementMethod());
        try {
            logger = Logger.getLogger();
            logger.accept(this);
            textLog.setText(logger.getLog());
        }
        catch(Exception e){
            textLog.setText(e.getMessage());
        }
    }

    @Override
    public void visit(String log) {
        textLog.setText(log);
    }
}
