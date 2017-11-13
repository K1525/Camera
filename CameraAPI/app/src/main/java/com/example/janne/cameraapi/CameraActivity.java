package com.example.janne.cameraapi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.id;
import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.example.janne.cameraapi.MainActivity.getCameraInstance;
import static com.example.janne.cameraapi.R.id.flash;

/**
 * Created by Janne on 31.10.2017.
 */

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        } else {
            start();
        }
        */

        /*Check permmisions to use camera and to write in internal storage*/
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        } else{
            start();
        }


        //SET BUTTONS THAT CHANGE CAMERA PARAMETERS HERE!!!

        final SeekBar ZoomValue = (SeekBar)findViewById(R.id.seekBar);
        ZoomValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int ZoomBarValue = ZoomValue.getProgress();

                Camera.Parameters params = mCamera.getParameters();
                params.setZoom(ZoomBarValue);
                mCamera.setParameters(params);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final ImageButton CameraFlash = (ImageButton) findViewById(flash);
        CameraFlash.setOnClickListener(new View.OnClickListener(){
            int flash = 0;

            @Override
            public void onClick(View v){
                if(flash == 0){
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(params);
                    CameraFlash.setImageResource(R.drawable.ic_flash_on_black_24dp);
                    flash = 1;
                }else{
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(params);
                    CameraFlash.setImageResource(R.drawable.ic_flash_off_black_24dp);
                    flash = 0;
                }
            }
        });

        final ImageButton HighDynamicRange = (ImageButton) findViewById(R.id.hdr);
        HighDynamicRange.setOnClickListener(new View.OnClickListener(){
            int hdr = 0;

            @Override
            public void onClick(View v){
                if(hdr == 0){
                    Camera.Parameters params = mCamera.getParameters();
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                    mCamera.setParameters(params);
                    HighDynamicRange.setImageResource(R.drawable.ic_hdr_on_black_24dp);
                    hdr = 1;
                }else{
                    Camera.Parameters params = mCamera.getParameters();
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                    mCamera.setParameters(params);
                    HighDynamicRange.setImageResource(R.drawable.ic_hdr_off_black_24dp);
                    hdr = 0;
                }
            }
        });

    }

    /*Check is permissions were granted*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                start();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    /*Get camera instance, start camera preview and listen for capture button*/
    private void start() {
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Listener for the Capture button
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    /*Save picture in file here*/
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            camera.startPreview();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };





    /*What happens if camera is paused*/
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    /*Release media recorder !!ONLY NEEDED IF APP RECORDS VIDEO!!*/
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    /*Release camera*/
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }




    /* Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /* Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        /*Get path where images are saved*/
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "KUVIA");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("KUVIA", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}


