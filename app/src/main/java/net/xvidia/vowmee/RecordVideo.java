package net.xvidia.vowmee;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import net.xvidia.vowmee.camera.CameraHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *  This activity uses the camera/camcorder as the A/V source for the {@link MediaRecorder} API.
 *  A {@link TextureView} is used as the camera preview which limits the code to API 14+. This
 *  can be easily replaced with a {@link android.view.SurfaceView} to run on older devices.
 */
public class RecordVideo extends Activity implements TextureView.SurfaceTextureListener {

    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    String mFileName;
    File videofile = null;
    private boolean flashOn = false;
    private boolean isRecording = false, isPreviewRunning = false;
    private static int previewWidth,previewHeight;
    private static final String TAG = "RecordVideo";
    private ImageButton switchCameraButton;
    private ImageButton flashOnOffButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        mPreview = (TextureView) findViewById(R.id.surface_view);
         switchCameraButton = (ImageButton) findViewById(R.id.button_switch_camera);
        flashOnOffButton = (ImageButton) findViewById(R.id.button_flash);
//       mPreview.setClickable(true);
        mPreview.setSurfaceTextureListener(this);
//        new MediaPrepareTask().execute(null, null, null);
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
//                onCaptureClick(v);
            }
        });

        flashOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flashOn) {
                    flashOn = false;
                }else {
                    flashOn = true;
                }
                startFlash();
            }
        });

        if(CameraHelper.getDefaultFrontFacingCameraInstance() == null)
            switchCameraButton.setVisibility(View.GONE);
        if(CameraHelper.getCameraId() == -1)
            CameraHelper.setDefaultCameraId();
        mCamera = CameraHelper.getDefaultCamera(CameraHelper.getCameraId());
    }

    private void startFlash(){
        PackageManager pm= getPackageManager();
        final Camera.Parameters p = mCamera.getParameters();
        if(CameraHelper.isFlashSupported(pm)){

            if (flashOn) {
                Log.i("info", "torch is turn on!");
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
                mCamera.startPreview();
            } else {
                Log.i("info", "torch is turn off!");
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
                mCamera.startPreview();
            }
        }else{
            flashOnOffButton.setEnabled(false);
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("No Camera Flash");
            alertDialog.setMessage("The device's camera doesn't support flash.");
            alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    Log.e("err", "The device's camera doesn't support flash.");
                   alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    private void switchCamera(){
        if(Camera.getNumberOfCameras() == 1){
            switchCameraButton.setVisibility(View.INVISIBLE);
        }
        else {
            if (isPreviewRunning) {
                mCamera.stopPreview();
            }
            //NB: if you don't release the current camera before switching, you app will crash
            mCamera.release();

            //swap the id of the camera to be used
            if (CameraHelper.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = CameraHelper.getDefaultFrontFacingCameraInstance();
                flashOnOffButton.setEnabled(false);
            } else {
                mCamera = CameraHelper.getDefaultBackFacingCameraInstance();
                PackageManager pm= getPackageManager();
                if(CameraHelper.isFlashSupported(pm)) {
                    flashOnOffButton.setEnabled(true);
                }else{
                    flashOnOffButton.setEnabled(false);
                }
            }
            try {
                //this step is critical or preview on new camera will no know where to render to
                setCameraDisplayOrientation(mCamera, previewWidth, previewHeight);
                mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link MediaRecorder} and {@link Camera}. When not recording,
     * it prepares the {@link MediaRecorder} and starts recording.
     *
     * @param view the view generating the event.
     */
    public void onCaptureClick(View view) {
        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            // setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean prepareVideoRecorder() {

        // BEGIN_INCLUDE (configure_preview)
        if(mCamera == null)
        mCamera = CameraHelper.getDefaultCameraInstance();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes,
                mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_QVGA);
       /* profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);*/
//        mCamera.setParameters(parameters);
//
//        try {
//            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
//            // with {@link SurfaceView}
//            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
//
//             /*  mCamera.setDisplayOrientation(90);*/
//
//        } catch (IOException e) {
//            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
//            return false;
//        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//                    mMediaRecorder.stop();
                    RecordVideo.this.finish();

                }
            }
        });
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
//        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);
        String path = Environment.getExternalStorageDirectory() + "/videoe_lapse.mp4";
        // Step 4: Set output file
        mMediaRecorder.setOutputFile(path);
        mMediaRecorder.setMaxDuration(10000);
        mMediaRecorder.setVideoFrameRate(10);
        // END_INCLUDE (configure_media_recorder)
//        mMediaRecorder.setPreviewDisplay(mPreview.getSurfaceTexture());
        mMediaRecorder.setCaptureRate(2);
        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        previewWidth = width;
        previewHeight = height;
//        if (!isPreviewRunning) {
        setCameraDisplayOrientation(mCamera, previewWidth, previewHeight);
        previewCamera(surface);
//            mPreview.setRotation(90.0f);
//        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        if (isPreviewRunning) {
//            mCamera.stopPreview();
//        }
//        setCameraDisplayOrientation(mCamera, width, height);
//        previewCamera(surface);

    }


    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        mCamera = CameraHelper.getDefaultCameraInstance();
        if(mCamera !=null) {
            mCamera.stopPreview();
            isPreviewRunning = false;
//            mCamera.release();
        }
            return true;
//        }
//        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void previewCamera(SurfaceTexture surface) {
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
            mPreview.setAlpha(1.0f);
            isPreviewRunning = true;
        } catch (Exception e) {
            Log.d("camera preview", "Cannot start preview", e);
        }
    }

    public void setCameraDisplayOrientation(Camera camera, int width, int height) {
        Camera.Parameters parameters = camera.getParameters();

        Camera.CameraInfo camInfo =
                new Camera.CameraInfo();
        Camera.getCameraInfo(CameraHelper.getCameraId(), camInfo);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        Camera.Size mCamerSize = CameraHelper.getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), width, height);


//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
        if(mCamerSize !=null) {
            parameters.setPreviewSize(mCamerSize.width, mCamerSize.height);
//        }else{
//            parameters.setPreviewSize(mCamerSize.height, mCamerSize.width);
//        }
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
                mPreview.setLayoutParams(new FrameLayout.LayoutParams(
                        mCamerSize.width, mCamerSize.height, Gravity.CENTER));
            }else{

                mPreview.setLayoutParams(new FrameLayout.LayoutParams(
                        mCamerSize.height, mCamerSize.width, Gravity.CENTER));
            }
        }
        mCamera.setParameters(parameters);
        camera.setDisplayOrientation(result);
//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
//            camera.setDisplayOrientation(90);
//        }
    }

    /**
     * Asynchronous task for preparing the {@link MediaRecorder} since it's a long blocking
     * operation.
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                RecordVideo.this.finish();
            }
            // inform the user that recording has started
            // setCaptureButtonText("Stop");

        }
    }

}
