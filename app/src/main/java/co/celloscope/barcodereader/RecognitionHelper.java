package co.celloscope.barcodereader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.Recognizer;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.InvalidLicenceKeyException;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.barcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.barcode.pdf417.Pdf417ScanResult;
import com.microblink.recognizers.settings.GenericRecognizerSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.view.recognition.RecognitionType;
import com.microblink.view.recognition.ScanResultListener;

/**
 * This class will take an image and recognize the Barcode
 *
 * @author assad
 */
public class RecognitionHelper {

    private static final String TAG = RecognitionHelper.class.getSimpleName();
    private static final String LicenseKey = "DMZBGSNR-JJVYIXSD-KNI2YQWF-2BZ4DIZV-M5JH2SDI-WFBVGWOJ-5OGZSBTD-KBWFNYND";
    private Recognizer mRecognizer = null;
    private final MainActivity directActivity;
    public static final String NAME = "NAME";
    private static final String PIN = "PIN";
    private static final String DOB = "DOB";
    private static final String BARCODE_TYPE = "BarcodeType";
    private static final String PDF417 = "PDF417";
    private static final String BARCODE_CONTENT = "BarcodeContent";
    private static final String NID = "NID";

    public RecognitionHelper(MainActivity activity) {
        this.directActivity = activity;
    }

    /**
     * Initialize recognizer; set license key;
     */
    void initializeRecognizer() {
        if (mRecognizer != null) {
            return;
        }
        mRecognizer = Recognizer.getSingletonInstance();
        Log.d(TAG, "initializeRecognizer() called with: " + "mRecognizer = [" + mRecognizer + "]");
        try {
            mRecognizer.setLicenseKey(directActivity, LicenseKey);
        } catch (InvalidLicenceKeyException e) {
            Log.e(TAG, "Failed to set licence key!");
            Toast.makeText(directActivity, "Failed to set licence key!",
                    Toast.LENGTH_LONG).show();
            directActivity.finish();
        }

        final RecognizerSettings[] recognizerSettings = new RecognizerSettings[1];
        if (PDF417.equals(directActivity.getIntent().getStringExtra(BARCODE_TYPE))) {
            recognizerSettings[0] = getPdf417RecognizerSettings();
        }
        mRecognizer.initialize(directActivity, getGenericRecognizerSettings(),
                recognizerSettings,
                new DirectApiErrorListener() {
                    @Override
                    public void onRecognizerError(Throwable throwable) {
                        Log.e(TAG, "Failed to initialize recognizer.",
                                throwable);
                        Toast.makeText(
                                directActivity,
                                "Failed to initialize recognizer. Reason: "
                                        + throwable.getMessage(),
                                Toast.LENGTH_LONG).show();
                        directActivity.finish();
                    }
                });
    }

    void recognizeBitmap(Bitmap bitmap) {
        Log.d(TAG, "recognizeBitmap() called with: " + "bitmap = [" + bitmap + "]" + "mRecognizer = [" + mRecognizer + "]");
        if (mRecognizer.getCurrentState() != Recognizer.State.READY) {
            Log.e(TAG, "Recognizer not ready!");
            return;
        }
        if (bitmap != null) {
            mRecognizer.setOrientation(Orientation.ORIENTATION_LANDSCAPE_RIGHT);
            final ProgressDialog pd = new ProgressDialog(directActivity);
            pd.setIndeterminate(true);
            pd.setMessage("Performing recognition");
            pd.setCancelable(false);
            pd.show();
            mRecognizer.recognize(bitmap, new ScanResultListener() {
                @Override
                public void onScanningDone(BaseRecognitionResult[] dataArray,
                                           RecognitionType recognitionType) {

                    if (dataArray != null && dataArray.length > 0) {
                        StringBuilder totalResult = new StringBuilder();

                        for (BaseRecognitionResult result : dataArray) {
                            if (result instanceof Pdf417ScanResult) {
                                Pdf417ScanResult pdf417Result = (Pdf417ScanResult) result;
                                totalResult
                                        .append("Barcode type: PDF417\nBarcode content:\n");
                                totalResult.append(pdf417Result.getStringData());
                                totalResult.append("\n\n");
                            }
                        }

                        final String scanResult = totalResult.toString();
                        directActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                                Intent intent = new Intent();
                                if (NID.equals(directActivity.getIntent().getStringExtra(BARCODE_CONTENT))) {
                                    intent.putExtra(NAME, scanResult
                                            .substring(scanResult.indexOf("<name>") + 6, scanResult.indexOf("</name>")).toUpperCase());
                                    intent.putExtra(PIN, scanResult
                                            .substring(scanResult.indexOf("<pin>") + 5, scanResult.indexOf("</pin>")).toUpperCase());
                                    intent.putExtra(DOB, scanResult
                                            .substring(scanResult.indexOf("<DOB>") + 5, scanResult.indexOf("</DOB>")).toUpperCase());
                                }
                                directActivity.setResult(directActivity.RESULT_OK, intent);
                                directActivity.finish();
                            }
                        });
                    } else {
                        Intent intent = new Intent();
                        directActivity.setResult(directActivity.RESULT_CANCELED, intent);
                        directActivity.finish();
                        directActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                            }
                        });
                    }
                }
            });
        }
    }

    void terminateRecognizer() {
        if (mRecognizer != null) {
            mRecognizer.terminate();
            mRecognizer = null;
        }
        Log.d(TAG, "terminateRecognizer() called with: " + "mRecognizer = [" + mRecognizer + "]");
    }


    private GenericRecognizerSettings getGenericRecognizerSettings() {
        GenericRecognizerSettings genSett = new GenericRecognizerSettings();
        genSett.setAllowMultipleScanResultsOnSingleImage(true);
        genSett.setNumMsBeforeTimeout(100000);
        genSett.setEnabled(true);
        return genSett;
    }

    private Pdf417RecognizerSettings getPdf417RecognizerSettings() {
        Pdf417RecognizerSettings pdf417Sett = new Pdf417RecognizerSettings();
        pdf417Sett.setUncertainScanning(true);
        pdf417Sett.setEnabled(true);
        pdf417Sett.setInverseScanning(false);
        pdf417Sett.setNullQuietZoneAllowed(true);
        return pdf417Sett;
    }
}
