package me.omarahmed.ebtikar.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Locale;

import me.omarahmed.ebtikar.R;
import me.omarahmed.ebtikar.data.Client;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CallEndedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "CallEndedActivity";

    public static final String CLIENT_AS_JSON = "CLIENT_AS_JSON";

    private TextView clientName, clientNumber, userAddress, userLocation;
    private Client client;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_ended);

        // Find ui
        clientName = findViewById(R.id.tv_client_name);
        clientNumber = findViewById(R.id.tv_client_number);
        userAddress = findViewById(R.id.tv_user_address);
        userLocation = findViewById(R.id.tv_user_latlong);

        Intent intent = getIntent();
        String clientJsonStr = intent.getStringExtra(CLIENT_AS_JSON);
        if (clientJsonStr == null || clientJsonStr.isEmpty()) {
            Toast.makeText(this, "Client json is empty or null", Toast.LENGTH_SHORT).show();
            finish();
        }

        // get clint object from json
        Gson gson = new Gson();
        client = gson.fromJson(clientJsonStr, Client.class);

        // set clint data
        clientName.setText(client.getName());
        clientNumber.setText(client.getMobile());

        // set dialog width and height
        setDialogWidthAndHeight();

        // get current location
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private void setDialogWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        getWindow().setLayout(width, WRAP_CONTENT);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Address address = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1).get(0);
            int maxAddressLine = address.getMaxAddressLineIndex();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= maxAddressLine; i++) sb.append(address.getAddressLine(i));
            userAddress.setText(String.format("Address:\n%s", sb.toString()));

            userLocation.setText(String.format("Latitude: %s\nLongitude: %s", address.getLatitude(), address.getLongitude()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
}
