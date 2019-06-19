package com.example.lazylapse;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import static android.os.SystemClock.sleep;


/**
 *  used the code presented in this article: https://www.vogella.com/tutorials/AndroidCamera/article.html
 *  had to add a textureView otherwise no preview and then no picture
 *  (preview is needed in camera (1) API in order to take pictures)
 */
public class Photographer extends Activity {
    protected final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = 0;
    private TextureView mTextureView;
    private Logger logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photographer);

        logger = Logger.getLogger();
        logger.addToLog("photographer created");

        Button capture = (Button) findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                takePicture();
            }
        });


        mTextureView = (TextureView) findViewById(R.id.textureView);

        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findBackFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);


            }
        }

        try {
            Boolean instant = this.getIntent().getBooleanExtra(Constant.INSTANT_PICTURE, false);
            if (instant) {
                logger.addToLog("try to take a picture");
            /*ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);*/
                try {
                    takePicture();
                } catch (Exception e) {
                    logger.addToLog(e.getMessage());
                }

            }
        }
        catch(Exception e){
            logger.addToLog(e.getMessage());
        }


    }

    private void takePicture(){
        logger.addToLog("taking a picture");
        try {
            SurfaceTexture surfaceTexture = new SurfaceTexture(10);
            camera.setPreviewTexture(surfaceTexture);
            logger.addToLog("preview set up");
        } catch (IOException e) {
            logger.addToLog(e.getMessage());
        }
        camera.startPreview();
        camera.takePicture(null, null, new PhotoHandler(getApplicationContext()));
    }


    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onPause() {
        /*if (camera != null) {
            camera.release();
            camera = null;
        }*/
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

}