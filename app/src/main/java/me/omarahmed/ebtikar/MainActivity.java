package me.omarahmed.ebtikar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button button;
    private ImageView imageView;
    private TextView txtView;
    private Bitmap bitmap;
    private BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imgview);
        txtView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectBarcode();
            }
        });

        loadImage();
        setupBarcodeDetector();
    }

    private void detectBarcode() {
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
        Barcode barcode = barcodes.valueAt(0);
        txtView.setText(barcode.rawValue);
        boolean isValid = URLUtil.isValidUrl(barcode.rawValue);
        Log.d(TAG, "Is url valid: " + isValid);

        if(isValid){
            // TODO: 18/05/2018 make remote request
        }
    }

    private void loadImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.image3, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        Log.d(TAG, "image height: " + imageHeight);
        Log.d(TAG, "image width: " + imageWidth);
        Log.d(TAG, "image type: " + imageType);


        if (imageWidth > 512 || imageHeight > 384)
            options.inSampleSize = BitmapUtil.calculateInSampleSize(options, 512, 384);

        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.image3,
                options);
        imageView.setImageBitmap(bitmap);
        setupBarcodeDetector();
    }

    private void setupBarcodeDetector() {
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .build();
        if (!barcodeDetector.isOperational()) {
            txtView.setText("Could not set up the detector!");
            return;
        } else {
            txtView.setText("set up the detector!");
        }
    }
}
