package me.omarahmed.ebtikar.ui.clients;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import me.omarahmed.ebtikar.R;
import me.omarahmed.ebtikar.data.Client;

/**
 * Created by Dell on 18/05/2018.
 */

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientViewHolder> {

    private List<Client> data;
    private ClientCallback callback;

    public ClientsAdapter(List<Client> data, ClientCallback callback) {
        this.data = data;
        this.callback = callback;
    }

    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_client, parent, false);

        // Return a new holder instance
        ClientViewHolder viewHolder = new ClientViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ClientViewHolder holder, int position) {
        holder.bindData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView number;
        ImageButton info;

        public ClientViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_client_name);
            number = itemView.findViewById(R.id.tv_client_number);
            info = itemView.findViewById(R.id.btn_info);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onClientInfoClicked(data.get(getAdapterPosition()));
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onClientClicked(data.get(getAdapterPosition()).getMobile());
                    }
                }
            });

        }

        public void bindData(Client client) {
            name.setText(client.getName());
            number.setText(client.getMobile());
        }
    }

    interface ClientCallback {
        void onClientClicked(String number);

        void onClientInfoClicked(Client client);
    }
}
