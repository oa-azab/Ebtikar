package me.omarahmed.ebtikar.ui.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;
import kotlin.Unit;
import me.omarahmed.ebtikar.R;

import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private CameraView cameraView;
    private ImageView imagePreview;
    private FloatingActionButton fabCapture;
    private Fotoapparat fotoapparat;
    private BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Find ui
        cameraView = findViewById(R.id.camera_view);
        imagePreview = findViewById(R.id.img_preview);
        fabCapture = findViewById(R.id.fab_capture);
        fabCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoResult photoResult = fotoapparat.takePicture();
                final File file = new File(getFilesDir(), "qrcodeimage.jpg");
                photoResult.saveToFile(file)
                        .whenDone(new WhenDoneListener<Unit>() {
                            @Override
                            public void whenDone(Unit unit) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                imagePreview.setVisibility(View.VISIBLE);
                                imagePreview.setImageBitmap(bitmap);
                                detect(bitmap);
                            }
                        });
            }
        });

        // setup camera
        setupCamera();

        // setup barcode detector
        setupBarcodeDetector();
    }

    private void setupBarcodeDetector() {
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        if (!barcodeDetector.isOperational())
            Toast.makeText(this, "Failed setting up barcode detector", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fotoapparat.stop();
    }

    private void setupCamera() {
        fotoapparat = Fotoapparat
                .with(this)
                .into(cameraView)           // view which will draw the camera preview
                .previewScaleType(ScaleType.CenterCrop)  // we want the preview to fill the view
                .lensPosition(back())       // we want back camera
                .focusMode(firstAvailable(  // (optional) use the first focus mode which is supported by device
                        autoFocus(),        // in case if continuous focus is not available on device, auto focus will be used
                        fixed()             // if even auto focus is not available - fixed focus mode will be used
                ))
                .flash(firstAvailable(      // (optional) similar to how it is done for focus mode, this time for flash
                        autoRedEye(),
                        autoFlash(),
                        torch()
                ))
                .build();
    }

    private void detect(Bitmap bitmap) {
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
        if (barcodes == null || barcodes.size() < 1) {
            Toast.makeText(this, "Detecting qr code failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Barcode barcode = barcodes.valueAt(0);
        boolean isValid = URLUtil.isValidUrl(barcode.rawValue);
        Log.d(TAG, "Is url valid: " + isValid);

        if (isValid) {
            finish();
            Toast.makeText(this, "data: " + barcode.rawValue, Toast.LENGTH_SHORT).show();
        }

    }
}
