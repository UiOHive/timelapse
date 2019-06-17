package com.example.lazylapse;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class Picturer extends AppCompatActivity implements View.OnClickListener {

    /**
     * interface variables
     */
    private String sLog; // contains all the log messages
    private Button buttonPicture;
    private TextView logText;
    /**
     * parameters
     */
    private String cameraId;

    /**
     * Object needed for Camera2 to work
     */
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder requestBuilder;
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            // Here, we create a CameraCaptureSession for camera preview.
            try {


                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);



                cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                // The camera is already closed
                                if (null == cameraDevice) {
                                    return;
                                }

                                // When the session is ready, we start displaying the preview.
                                captureSession = cameraCaptureSession;

                                // Auto focus for the first picture, need to be change later for all
                                // other picture
                                requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_AUTO);
                                requestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON); //lock the AWB algorithm to its last calculated value
                            }

                            @Override
                            public void onConfigureFailed(
                                    @NonNull CameraCaptureSession cameraCaptureSession) {
                                addToLog("Failed setting up captureSession");
                            }
                        }, null
                );

            } catch(Exception e) {
                addToLog(e.getMessage());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
            addToLog("disconnected camera");

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            addToLog("encountered error when opening the camera");
            camera.close();
            cameraDevice = null;



        }

    };
        private CaptureRequest.Builder captureRequestBuilder;

        /**
         * state parameter, int used to communicate between callBacks and methods
         */
        private int state;
        private static final int STATE_WAITING_LOCK = 1;
        private CameraCaptureSession captureSession;
        private ImageReader imageReader;
        private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), new File(getApplication().getApplicationContext().getFilesDir(),"test.jpg")));
            }
        };
        private SharedPreferences parameters;

        private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
            private void process(CaptureResult result) {
                switch (state) {
                    case STATE_WAITING_LOCK: {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) {
                            requestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON); //lock the AWB algorithm to its last calculated value
                            captureStillPicture();
                        } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                                CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                            // CONTROL_AE_STATE can be null on some devices
                            requestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON); //lock the AWB algorithm to its last calculated value
                            captureStillPicture();

                        }
                    }
                }

            }
        };
    private CameraManager cameraManager;

    private void captureStillPicture() {
        try{
        if(null == cameraDevice) {
            return;
        }
        // This is the CaptureRequest.Builder that we use to take a picture.
        final CaptureRequest.Builder captureBuilder =
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());

        // Use the same AE and AF modes as the preview.
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_AUTO); //should not change the focus hopefully
        // Orientation
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0); //should calculate a good orientation but for my sanity let's say no

        CameraCaptureSession.CaptureCallback CaptureCallback
                = new CameraCaptureSession.CaptureCallback() {

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                addToLog("Saved file");
            }
        };

        captureSession.stopRepeating();
        captureSession.abortCaptures();
        captureSession.capture(captureBuilder.build(), CaptureCallback, null);
    } catch (CameraAccessException e) {
        e.printStackTrace();
        }
    }


    private int focus;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_picturer);

            /**
             * use String to store the log we want to display to the user, add message and desplay it
             * by using {@link addToLog}.
             */
            logText = (TextView) findViewById(R.id.textLog);
            sLog = "";

            /**
             * button used to take picture
             */
            buttonPicture = (Button) findViewById(R.id.buttonPicture);
            buttonPicture.setOnClickListener(this);
            try {
                startBackgroundThread();
            }
            catch(Exception e){
                addToLog("error when setting up the backgroundthread" + e.getMessage());
            }
            /**
             * we gather parameters needed to start taking pictures and set up the camera
             */
            setUpCamera();
            focus = -1; //not focused

        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonPicture:
                    takePicture();
            }

        }


        private void setUpCamera() {
            getParameters();
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {

                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                imageReader.setOnImageAvailableListener(
                        onImageAvailableListener, null /*supposed to be a BackgroundHandler*/);
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, stateCallback, backgroundHandler /*supposed to be a BackgroundHandler*/);
                    addToLog("camera " + cameraId + " has been set up!!!");
                } else {
                    addToLog("Permission to use camera not granted :(");
                }



            } catch (Exception e) {
                addToLog("error when setting up the camera: " + e.getMessage());
            }

        }

        /**
         * retrieve parameters from user preference
         */
        private void getParameters() {
            parameters = PreferenceManager.getDefaultSharedPreferences(this);
            cameraId = parameters.getString("cameraId", "0");
        }

        /**
         * take a single picture
         */
        private void takePicture() {
            addToLog("Picture taken");
            if (focus == -1) { // -1 is not a real focus so it will never get this value by our process unless we assign it ourselves as default value
                // do the focusing here, it will be donne once each time we start capturing images
                lockFocus();

            }

        }

        private void lockFocus() {
            try {
                // This is how to tell the camera to lock focus.
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);
                // Tell #captureCallback to wait for the lock.
                state = STATE_WAITING_LOCK;
                captureSession.capture(captureRequestBuilder.build(), captureCallback,
                        null /*supposed to be a BackgroundHandler*/);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        /**
         * Add new message to display in the log TextView do not erase anything yet
         *
         * @param text
         */
        private void addToLog(String text) {
            sLog = text + "\n";
            logText.setText(sLog);
        }
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.run();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    }
