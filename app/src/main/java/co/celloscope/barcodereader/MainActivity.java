package co.celloscope.barcodereader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final RecognitionHelper recognitionHelper = new RecognitionHelper(
            this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deleteBarcodeFile();
        recognitionHelper.initializeRecognizer();
        dispatchTakePictureIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteBarcodeFile();
        recognitionHelper.terminateRecognizer();
    }

    private void deleteBarcodeFile() {
        File outputMediaFile = getOutputMediaFile();
        boolean isDeleted = outputMediaFile != null && outputMediaFile.delete();
        Log.d(TAG, String.valueOf(isDeleted));
    }

    void dispatchTakePictureIntent() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri barcodeImageUri = Uri.fromFile(getOutputMediaFile());
//        if (barcodeImageUri != null) {
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, barcodeImageUri);
//            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//        } else {
//            Toast.makeText(MainActivity.this, "Failed to create directory", Toast.LENGTH_SHORT).show();
//        }
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                Bundle extras = data.getExtras();
                File imageBitmap = (File) extras.get("image");
                if (resultCode == RESULT_OK && imageBitmap != null) {
                    recognitionHelper.recognizeBitmap(BitmapFactory.decodeFile(imageBitmap.getPath()));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BarcodeReader");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("BarcodeReader", "failed to create directory");
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + "tempNID.jpg");
    }
}
