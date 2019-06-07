package com.example.lazylapse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class Controller extends AppCompatActivity {
    private ImageButton settingsButton;
    private ImageButton cameraButton;
    private Photographer photographer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
                startActivity(i);
            }});
    }
}
