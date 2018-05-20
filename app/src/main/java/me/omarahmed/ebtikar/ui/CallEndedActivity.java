package me.omarahmed.ebtikar.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import me.omarahmed.ebtikar.R;
import me.omarahmed.ebtikar.data.Client;

public class CallEndedActivity extends AppCompatActivity {

    private static final String TAG = "CallEndedActivity";

    public static final String CLIENT_AS_JSON = "CLIENT_AS_JSON";

    private TextView clientName, clientNumber;
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_ended);


        // Find ui
        clientName = findViewById(R.id.tv_client_name);
        clientNumber = findViewById(R.id.tv_client_number);

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
    }

    private void setDialogWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        getWindow().setLayout(width, (int) (height * 0.3));
    }
}
