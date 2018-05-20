package me.omarahmed.ebtikar.ui.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import me.omarahmed.ebtikar.ui.camera.CameraActivity;
import me.omarahmed.ebtikar.util.BitmapUtil;
import me.omarahmed.ebtikar.R;
import me.omarahmed.ebtikar.data.Client;
import me.omarahmed.ebtikar.remote.EbtikarService;
import me.omarahmed.ebtikar.ui.clients.ClientsActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button btnTakePicture, btnScan;
    private ImageView imageView;

    private Bitmap bitmap;
    private BarcodeDetector barcodeDetector;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imgview);
        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });
        btnTakePicture = findViewById(R.id.btn_take_picture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermissions();
            }
        });
        loadImage();
        setupBarcodeDetector();
        setupProgressDialog();
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Permissions Required");
                    builder.setMessage("Please grant permission to continue using the app");
                    builder.create().show();
                } else {
                    detectBarcode();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }
    private void checkCameraPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Permissions Required");
                    builder.setMessage("Please grant permission to continue using the app");
                    builder.create().show();
                } else {
                    captureImage();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    private void detectBarcode() {
        showProgressDialog();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

        if (barcodes == null || barcodes.size() < 1) {
            hideProgressDialog();
            Toast.makeText(this, "Failed to detect QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        Barcode barcode = barcodes.valueAt(0);
        boolean isValid = URLUtil.isValidUrl(barcode.rawValue);
        Log.d(TAG, "Is url valid: " + isValid);

        if (!isValid) {
            hideProgressDialog();
            Toast.makeText(this, "Url not valid: " + barcode.rawValue, Toast.LENGTH_SHORT).show();
            return;
        }

        makeHttpRequest(barcode.rawValue);
    }

    private void makeHttpRequest(String url) {
        int lastIndex = url.lastIndexOf('/') + 1;
        String endpointStr = url.substring(lastIndex);
        url = url.substring(0, lastIndex);
        Log.d(TAG, "baseUrl: " + url);
        Log.d(TAG, "endPoint: " + endpointStr);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        EbtikarService ebtikarService = retrofit.create(EbtikarService.class);

        final Call<String> call = ebtikarService.getClients(endpointStr);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    hideProgressDialog();
                    String res = response.body();
                    Log.d(TAG, "res: " + res);
                    JSONObject jsonObject = new JSONObject(res);
                    JSONArray jsonArray = jsonObject.getJSONArray("clients");
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Client>>() {
                    }.getType();

                    List<Client> clients = gson.fromJson(jsonArray.toString(), listType);
                    Log.d(TAG, "size: " + clients.size());

                    if (clients.size() > 0) {
                        Intent intent = new Intent(MainActivity.this, ClientsActivity.class);
                        intent.putExtra(ClientsActivity.BUNDLE_ARGS_CLIENTS_RESPONSE, jsonArray.toString());
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "error: " + e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                hideProgressDialog();
                Toast.makeText(MainActivity.this, "Network error please try again", Toast.LENGTH_SHORT).show();
            }
        });
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                bitmap = imageBitmap;
                imageView.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(this, "Couldn't load image please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Scanning QR code ...");
        progressDialog.setCancelable(false);
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
