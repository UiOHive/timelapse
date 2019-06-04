package com.example.lazylapse;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class Photographer extends AppCompatActivity {
    private CameraManager cameraManager;
    private String[] listCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photographer);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            listCamera = cameraManager.getCameraIdList();
            Toast toast = Toast.makeText(this, Integer.toString(listCamera.length), Toast.LENGTH_LONG);
            toast.show();
        } catch (CameraAccessException e) {
            Toast toast = Toast.makeText(this, "erreur lors de l'initialisation de Camera Manager", Toast.LENGTH_LONG);
            toast.show();
        }
    }

}
