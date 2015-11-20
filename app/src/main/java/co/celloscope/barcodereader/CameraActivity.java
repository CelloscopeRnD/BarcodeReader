package co.celloscope.barcodereader;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {
    private final RecognitionHelper recognitionHelper = new RecognitionHelper(this);
    private Camera mCamera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        recognitionHelper.initializeRecognizer();
        mCamera = getCameraInstance();
        mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        preview.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                recognitionHelper.recognizeBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                            }
                        });
                    }
                }
        );
    }

    public void releaseCamera() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void startCameraPreview(){
        mCamera.startPreview();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognitionHelper.terminateRecognizer();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}