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
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;
import me.omarahmed.ebtikar.R;

import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static me.omarahmed.ebtikar.util.BitmapUtil.calculateInSampleSize;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private CameraView cameraView;
    private FloatingActionButton fabCapture;
    private Fotoapparat fotoapparat;
    private BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Find ui
        cameraView = findViewById(R.id.camera_view);
        fabCapture = findViewById(R.id.fab_capture);
        fabCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoResult photoResult = fotoapparat.takePicture();
                photoResult.toBitmap()
                        .whenDone(new WhenDoneListener<BitmapPhoto>() {
                            @Override
                            public void whenDone(BitmapPhoto bitmapPhoto) {
                                detect(bitmapPhoto.bitmap);
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
                // .frameProcessor(frameProcessor)   // (optional) receives each frame from preview stream
                .build();
    }

    private FrameProcessor frameProcessor = new FrameProcessor() {
        @Override
        public void process(Frame frame) {
            byte[] image = frame.getImage();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(image, 0, image.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 500, 500);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bmp1 = BitmapFactory.decodeByteArray(image, 0, image.length, options);

            detect(bmp1);

        }
    };

    private void detect(Bitmap bitmap) {
        com.google.android.gms.vision.Frame frame = new com.google.android.gms.vision.Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
        Barcode barcode = barcodes.valueAt(0);
        boolean isValid = URLUtil.isValidUrl(barcode.rawValue);
        Log.d(TAG, "Is url valid: " + isValid);

        if (isValid) {
            finish();
            Toast.makeText(this, "data: " + barcode.rawValue, Toast.LENGTH_SHORT).show();
        }

    }
}
