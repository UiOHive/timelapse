package com.example.lazylapse.Photo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.lazylapse.Constant;
import com.example.lazylapse.Interface.Logger;
import com.example.lazylapse.R;
import java.io.IOException;
import java.util.List;


/**
 *  used the code presented in this article: {@linkplain https://www.vogella.com/tutorials/AndroidCamera/article.html}
 *  had to add a textureView otherwise no preview and then no picture
 *  (preview is needed in camera (1) API in order to take pictures)
 */
public class Photographer extends Activity {
    protected final static String DEBUG_TAG = "MakePhotoActivity";
    private Camera camera;
    private int cameraId = 0;
    private TextureView mTextureView;
    private Logger logger;

    /**
     * When the activity is initiated by this method, we setup different things like {@link Logger}used
     * to display and save the steps taken by the activity in order to capture a picture
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photographer);

        logger = Logger.getLogger();
        logger.appendLog("photographer created");

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
                logger.appendLog("try to take a picture");
            /*ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);*/
                try {
                    takePicture();
                } catch (Exception e) {
                    logger.appendLog(e.getMessage());
                }

            }
        }
        catch(Exception e){
            logger.appendLog(e.getMessage());
        }


    }

    /**
     * create a SurfaceTexture to display the image(which we actualy do not display in the activity
     * but that is needed either way), set the size parameter and launch the capture.
     */
    private void takePicture() {
        try {
            SurfaceTexture surfaceTexture = new SurfaceTexture(10);
            camera.setPreviewTexture(surfaceTexture);
            logger.appendLog("preview set up");
        } catch (IOException e) {
            logger.appendLog(e.getMessage());
        }
        camera.startPreview();

        Camera.Parameters params = camera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for(int i=0;i<sizes.size();i++)
        {
            if(sizes.get(i).width > size.width)
                size = sizes.get(i);
        }
        params.setPictureSize(size.width, size.height);

        logger.appendLog("taking a picture");
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        camera.setParameters(params);

        camera.takePicture(null, null, new PhotoHandler(getApplicationContext()));

    }

    /**
     * functuion used to choose the back camera to take the picture
     * @return
     */
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
        camera.release();
        camera = null;
        Log.i("PAUSE", "camera released trying to kill activity");
        super.onPause();
        finish();
    }
}