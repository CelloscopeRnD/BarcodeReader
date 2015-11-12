package co.celloscope.barcodereader;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int CHOSE_PICTURE = 1;
    FloatingActionButton fab;
    private static final String fileName = "good.jpg";
    private final RecognitionHelper recognitionHelper = new RecognitionHelper(
            this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), CHOSE_PICTURE);
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
            case CHOSE_PICTURE:
                if (resultCode == RESULT_OK && null != data) {
                    String selectedImagePath = getPath(data.getData());
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
