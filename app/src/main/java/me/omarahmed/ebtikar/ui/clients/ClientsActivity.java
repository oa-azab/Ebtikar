package me.omarahmed.ebtikar.ui.clients;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import me.omarahmed.ebtikar.R;
import me.omarahmed.ebtikar.data.Client;

public class ClientsActivity extends AppCompatActivity {

    private static final String TAG = "ClientsActivity";
    public static final String BUNDLE_ARGS_CLIENTS_RESPONSE = "BUNDLE_ARGS_CLIENTS_RESPONSE";

    private List<Client> clientsData;
    private RecyclerView recyclerView;
    private ClientsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);

        Intent intent = getIntent();
        String response = intent.getStringExtra(BUNDLE_ARGS_CLIENTS_RESPONSE);
        if (response == null || response.isEmpty()) {
            Toast.makeText(this, "response is null or empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Client>>() {
        }.getType();
        clientsData = gson.fromJson(response, listType);

        recyclerView = findViewById(R.id.rv_clients);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ClientsAdapter(clientsData, clientCallback);
        recyclerView.setAdapter(adapter);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private ClientsAdapter.ClientCallback clientCallback = new ClientsAdapter.ClientCallback() {
        @Override
        public void onClientClicked(String number) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        }

        @Override
        public void onClientInfoClicked(Client client) {
            ClientInfoSheet clientInfoSheet = ClientInfoSheet.newInstance(client);
            clientInfoSheet.show(getSupportFragmentManager(), "ClientInfoSheet");
        }
    };
}
