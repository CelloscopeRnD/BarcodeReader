package co.celloscope.barcodereader;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_PICTURE = 1;
    private final RecognitionHelper recognitionHelper = new RecognitionHelper(
            this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.pickPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_PICTURE);
            }
        });

        findViewById(R.id.takePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }

            private void dispatchTakePictureIntent() {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognitionHelper.terminateRecognizer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recognitionHelper.initializeRecognizer();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICTURE:
                if (resultCode == RESULT_OK && null != data) {
                    String selectedImagePath = getPath(data.getData());
                    recognitionHelper.recognizeBitmap(BitmapFactory.decodeFile(selectedImagePath));
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK && null != data) {
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = managedQuery(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection, null, null, null);
                    int column_index_data = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToLast();

                    String selectedImagePath = cursor.getString(column_index_data);
                    recognitionHelper.recognizeBitmap(BitmapFactory.decodeFile(selectedImagePath));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }
}
