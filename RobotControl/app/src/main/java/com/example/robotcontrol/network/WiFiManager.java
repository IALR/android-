package com.example.robotcontrol.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;

import androidx.annotation.NonNull;

/**
 * WiFi Manager for connecting to robot WiFi networks
 */
public class WiFiManager {
    
    private Context context;
    private android.net.wifi.WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    
    public interface ConnectionCallback {
        void onConnected(Network network);
        void onConnectionFailed(String error);
        void onDisconnected();
    }
    
    public WiFiManager(Context context) {
        this.context = context;
        this.wifiManager = (android.net.wifi.WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    
    /**
     * Connect to WiFi network
     * Different methods for different Android versions
     */
    public void connectToWiFi(String ssid, String password, ConnectionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Use WifiNetworkSpecifier
            connectToWiFiModern(ssid, password, callback);
        } else {
            // Android 9 and below - Use WifiConfiguration
            connectToWiFiLegacy(ssid, password, callback);
        }
    }
    
    /**
     * Modern WiFi connection (Android 10+)
     */
    private void connectToWiFiModern(String ssid, String password, ConnectionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password);
            
            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();
            
            NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier);
            
            NetworkRequest networkRequest = requestBuilder.build();
            
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                    callback.onConnected(network);
                }
                
                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    callback.onConnectionFailed("Network unavailable");
                }
                
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    callback.onDisconnected();
                }
            };
            
            connectivityManager.requestNetwork(networkRequest, networkCallback);
        }
    }
    
    /**
     * Legacy WiFi connection (Android 9 and below)
     */
    private void connectToWiFiLegacy(String ssid, String password, ConnectionCallback callback) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);
        
        int netId = wifiManager.addNetwork(wifiConfig);
        if (netId != -1) {
            wifiManager.disconnect();
            boolean enabled = wifiManager.enableNetwork(netId, true);
            boolean reconnected = wifiManager.reconnect();
            
            if (enabled && reconnected) {
                callback.onConnected(null);
            } else {
                callback.onConnectionFailed("Failed to connect to network");
            }
        } else {
            callback.onConnectionFailed("Failed to add network configuration");
        }
    }
    
    /**
     * Disconnect from current WiFi
     */
    public void disconnect() {
        if (wifiManager != null) {
            wifiManager.disconnect();
        }
    }
    
    /**
     * Check if connected to WiFi
     */
    public boolean isConnectedToWiFi() {
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && 
                       capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && 
                       networkInfo.getType() == ConnectivityManager.TYPE_WIFI && 
                       networkInfo.isConnected();
            }
        }
        return false;
    }
    
    /**
     * Get current WiFi SSID
     */
    public String getCurrentSSID() {
        if (wifiManager != null) {
            android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                // Remove quotes from SSID
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    return ssid.substring(1, ssid.length() - 1);
                }
                return ssid;
            }
        }
        return null;
    }
}
