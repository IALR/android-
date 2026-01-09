package com.example.robotcontrol.adapters;

import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.R;

import java.util.List;

public class WifiNetworkAdapter extends RecyclerView.Adapter<WifiNetworkAdapter.NetworkViewHolder> {

    public interface OnNetworkClickListener {
        void onNetworkClick(ScanResult network);
    }

    private final List<ScanResult> networks;
    private final OnNetworkClickListener listener;

    public WifiNetworkAdapter(List<ScanResult> networks, OnNetworkClickListener listener) {
        this.networks = networks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NetworkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new NetworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworkViewHolder holder, int position) {
        ScanResult network = networks.get(position);

        String ssid = network.SSID != null ? network.SSID : "";
        String bssid = network.BSSID != null ? network.BSSID : "";

        holder.deviceName.setText(ssid.isEmpty() ? "Hidden network" : ssid);
        holder.deviceAddress.setText(bssid);

        holder.itemView.setOnClickListener(v -> listener.onNetworkClick(network));
    }

    @Override
    public int getItemCount() {
        return networks.size();
    }

    static class NetworkViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceAddress;

        public NetworkViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceAddress = itemView.findViewById(R.id.deviceAddress);
        }
    }
}
