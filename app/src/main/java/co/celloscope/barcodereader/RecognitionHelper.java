package co.celloscope.barcodereader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

        mRecognizer.initialize(directActivity, getGenericRecognizerSettings(),
                new RecognizerSettings[]{getPdf417RecognizerSettings()},
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
            directActivity.fab.setEnabled(false);
            final ProgressDialog pd = new ProgressDialog(directActivity);
            pd.setIndeterminate(true);
            pd.setMessage("Performing recognition");
            pd.setCancelable(false);
            pd.show();
            // recognize image
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

                        // raise dialog with barcode result on UI thread
                        final String scanResult = totalResult.toString();
                        directActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                directActivity.fab.setEnabled(true);
                                pd.dismiss();

                                AlertDialog.Builder b = new AlertDialog.Builder(
                                        directActivity);
                                b.setTitle("Scan result")
                                        .setMessage(scanResult)
                                        .setCancelable(false)
                                        .setNeutralButton(
                                                "OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialog,
                                                            int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                            }
                        });
                    } else {
                        Toast.makeText(directActivity, "Nothing scanned!",
                                Toast.LENGTH_SHORT).show();
                        // enable button again
                        directActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                directActivity.fab.setEnabled(true);
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
