package co.celloscope.barcodereader;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private final RecognitionHelper recognitionHelper = new RecognitionHelper(
            this);
    private Uri barcodeImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognitionHelper.initializeRecognizer();
        dispatchTakePictureIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognitionHelper.terminateRecognizer();
    }


    void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        barcodeImageUri = Uri.fromFile(getOutputMediaFile());
        if (barcodeImageUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, barcodeImageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(MainActivity.this, "Failed to create directory", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                File file = getOutputMediaFile();
                if (resultCode == RESULT_OK && file != null) {
                    recognitionHelper.recognizeBitmap(BitmapFactory.decodeFile(file.getPath()));
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
