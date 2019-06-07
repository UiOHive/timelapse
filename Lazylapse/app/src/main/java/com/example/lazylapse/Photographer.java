package com.example.lazylapse;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Map;

/**
 * Class/activity providing control over camera using camera2 which implements a more hardware side than previous API
 * which is very relevant to this app
 * @author: Valentin HUE, ENSG
 */
public class Photographer extends AppCompatActivity {
    private CameraManager cameraManager;
    private String[] listCamera;
    private SharedPreferences preferences;

    private Map listPref;

    private CameraCaptureSession captureSession;
    private Size mPreviewSize;
    private Size outSize;
    private TextureView textureView;

    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;


    private Handler backgroundHandler;
    private HandlerThread handlerThread;

    /**
     * onCreate method from AppCompactActivity, override in order to define what should happen on this activity launch.
     * Used here to initialize the CameraManager which is used to obtain information on all cameras and to obtain a CameraDevice object.
     *
     * @param savedInstanceState used to restore to a previous state, always null if run for the first time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photographer);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            listCamera = cameraManager.getCameraIdList();
            Toast toast = Toast.makeText(this, Integer.toString(listCamera.length), Toast.LENGTH_LONG);
            toast.show();
        } catch (CameraAccessException e) {
            Toast toast = Toast.makeText(this, "erreur lors de l'initialisation de Camera Manager", Toast.LENGTH_LONG);
            toast.show();
        }
        textureView = (TextureView) findViewById(R.id.textureView);

        textureView.setSurfaceTextureListener(surfaceTextureListener);

    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * Used to open camera choosen in the preference activity
     * @throws CameraAccessException
     */
    private void openCamera() throws CameraAccessException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cameraId = prefs.getString("cameraId", "0");
        CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        outSize = map.getOutputSizes(SurfaceTexture.class)[0];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        cameraManager.openCamera(cameraId, stateCallback, null);

    }

    /**
     * One of the callback to implement in order to use camera2 API handle events related to CameraDevice(s).
     * They are trigger when a CameraDevice changes state, different callback are triggered according to the type
     * of the new state. {@link https://developer.android.com/reference/android/hardware/camera2/CameraDevice.StateCallback.html}
     */
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        /**
         * Called when a camera device has finished opening.
         * @param camera This will be the camera which trigger this callback
         */
        @Override
        public void onOpened(@android.support.annotation.NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                startCameraPreview(); // startCameraPreview() throws CameraAccessException
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        /**
         * The method called when a camera device is no longer available for use.
         * This callback may be called instead of onOpened(CameraDevice) if opening the camera fails.
         * @param camera
         */
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        /**
         * The method called when a camera device has encountered a serious error. We "clean up" the camera object
         * could be a good idea to include an user notification.
         * @param camera
         * @param error
         */
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    /**
     * used to intialize the preview. {@code CameraDevice.TEMPLATE_PREVIEW} is used to tell the
     * camera hardware that we want to prioritize frame rate over picture quality.
     * @throws CameraAccessException
     */
    private void startCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(outSize.getWidth(),outSize.getHeight());
        Surface surface = new Surface(texture);
        captureRequestBuilder =  cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

        captureRequestBuilder.addTarget(surface);

        /**
         * CameraDevice.createCaptureSession "A configured capture session for a CameraDevice,
         * used for capturing images from the camera or reprocessing images captured from the camera
         * in the same session previously.", basically it allow to tell where to send pictures (list
         * of surfaces) and what should append on states change.
         */
        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
            /**
             * Called when the session is ready, and can start processing request (taking pictures)
             * @param session
             */
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }
                captureSession = session;
                try {
                    updatePreview(); //throws CameraAccessException
                }
                catch (CameraAccessException e) {
                    e.printStackTrace();
                }

            }

            /**
             * self-explanatory, could display a Toast in order to notify the user of the error/ a log
             * @param session
             */
            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        },null);

    }

    private void getParameters(){
        listPref = preferences.getAll();
    }

    /**
     * Configure repeating request in order to preview the camera, the Surface object target is
     * created when captureSession is initialized (not here). We defined CONTROL_MODE to autofocus
     * but there are other mode available.
     * @throws CameraAccessException
     */
    private void updatePreview() throws CameraAccessException {
        if(cameraDevice == null){
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
        captureSession.setRepeatingRequest(captureRequestBuilder.build(),null,backgroundHandler);
        /*
        setRepeatingRequest's capture have lower priority than simple single capture request which
        means that when a capture request is emitted, repeating request will be paused until the
        single capture request has been treated.

        backgroundHandler represent the backgroundThread handler in which the requests will be executed.
        */
    }

    /**
     * method called when we reopen activity, we restart the thread allowing to display the preview again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(textureView.isAvailable()){
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    /**
     * the backgroundThread is used to run the repeated request for the preview
     */
    private void startBackgroundThread() {
        handlerThread = new HandlerThread("CameraBackgroundThread");
        handlerThread.start();

        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    /**
     * activity onPause method, executed when activity goes into background, stop the preview by calling stopBackgroundThread
     */
    @Override
    protected void onPause() {
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();

    }

    /**
     * stop the background thread, it is used to stop preview when the app goes in the background.
     * @throws InterruptedException
     */
    private void stopBackgroundThread() throws InterruptedException {
        handlerThread.quitSafely();
        handlerThread.join();

        backgroundHandler = null;
        handlerThread = null;
    }
}
