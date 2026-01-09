package com.example.robotcontrol;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;

public class WiFiManager {
    private Context context;
    private WifiManager wifiManager;

    public WiFiManager(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isWiFiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    public void enableWiFi() {
        if (wifiManager != null && !isWiFiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public boolean connectToNetwork(String ssid, String password) {
        if (wifiManager == null) return false;

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.preSharedKey = "\"" + password + "\"";
        config.status = WifiConfiguration.Status.ENABLED;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        int networkId = wifiManager.addNetwork(config);
        if (networkId == -1) {
            return false;
        }

        return wifiManager.enableNetwork(networkId, true);
    }

    public String getConnectedSSID() {
        return wifiManager.getConnectionInfo().getSSID().replace("\"", "");
    }

    public boolean isConnectedToNetwork(String ssid) {
        try {
            String connectedSSID = getConnectedSSID();
            return connectedSSID.equals(ssid);
        } catch (Exception e) {
            return false;
        }
    }
}

