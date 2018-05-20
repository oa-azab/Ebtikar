package me.omarahmed.ebtikar.ui.clients;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import me.omarahmed.ebtikar.R;
import me.omarahmed.ebtikar.data.Client;
import me.omarahmed.ebtikar.data.Interest;
import me.omarahmed.ebtikar.data.Language;
import me.omarahmed.ebtikar.receivers.PhonecallReceiver;

/**
 * Created by Dell on 18/05/2018.
 */

public class ClientInfoSheet extends BottomSheetDialogFragment {

    private TextView name, number, age, interests, languages;
    private Button call;
    public Client client;

    public static ClientInfoSheet newInstance(Client client) {
        ClientInfoSheet clientInfoSheet = new ClientInfoSheet();
        clientInfoSheet.client = client;
        return clientInfoSheet;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_client, container,
                false);

        // get the views and attach the listener
        name = view.findViewById(R.id.tv_client_name);
        number = view.findViewById(R.id.tv_client_number);
        age = view.findViewById(R.id.tv_client_age);
        interests = view.findViewById(R.id.tv_client_interests);
        languages = view.findViewById(R.id.tv_client_languages);

        call = view.findViewById(R.id.btn_call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callClient();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name.setText(client.getName());
        number.setText(client.getMobile());
        age.setText(String.format("Age: %s", client.getAge()));

        StringBuilder sbInterests = new StringBuilder();
        List<Interest> clientIntresets = client.getInterests();
        for (int i = 0; i < clientIntresets.size(); i++) {
            sbInterests.append(clientIntresets.get(i).getTitle());
            sbInterests.append(i == clientIntresets.size() - 1 ? "." : ", ");
        }
        interests.setText(String.format("Interests: \n%s", sbInterests.toString()));

        StringBuilder sbLanguages = new StringBuilder();
        List<Language> clientLanguages = client.getLanguages();
        for (int i = 0; i < clientLanguages.size(); i++) {
            sbLanguages.append(clientLanguages.get(i).getTitle());
            sbLanguages.append(": ");
            sbLanguages.append(clientLanguages.get(i).getLevel());
            sbLanguages.append("\n");
        }
        languages.setText(String.format("Languages: \n%s", sbLanguages.toString()));
    }

    private void callClient() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + client.getMobile()));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
        PhonecallReceiver.client = client;
    }
}
