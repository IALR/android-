package com.example.robotcontrol.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

/**
 * WiFi Manager for connecting to robot WiFi networks
 */
public class WiFiManager {
    
    private Context context;
    private android.net.wifi.WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback activeNetworkCallback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
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
        // Fast-path: if we're already on that SSID, don't request again.
        try {
            String current = getCurrentSSID();
            if (current != null && current.equals(ssid)) {
                callback.onConnected(null);
                return;
            }
        } catch (Exception ignored) {
        }

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
            // Clean up any previous request.
            disconnect();

            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password);
            
            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();
            
            NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    // ESP32 AP usually has no Internet; avoid waiting for Internet validation.
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(wifiNetworkSpecifier);
            
            NetworkRequest networkRequest = requestBuilder.build();

            final Runnable timeout = () -> {
                try {
                    if (activeNetworkCallback != null) {
                        connectivityManager.unregisterNetworkCallback(activeNetworkCallback);
                    }
                } catch (Exception ignored) {
                }
                activeNetworkCallback = null;
                callback.onConnectionFailed("Connection timed out. Accept the WiFi prompt and try again.");
            };

            mainHandler.postDelayed(timeout, 15000);
            
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    mainHandler.removeCallbacks(timeout);
                    connectivityManager.bindProcessToNetwork(network);
                    callback.onConnected(network);
                }
                
                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    mainHandler.removeCallbacks(timeout);
                    callback.onConnectionFailed("Network unavailable");
                }
                
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    callback.onDisconnected();
                }
            };
            
            activeNetworkCallback = networkCallback;
            try {
                connectivityManager.requestNetwork(networkRequest, networkCallback);
            } catch (SecurityException se) {
                mainHandler.removeCallbacks(timeout);
                activeNetworkCallback = null;
                callback.onConnectionFailed("WiFi permission denied. Grant Nearby WiFi / Location permission and try again.");
            } catch (IllegalArgumentException iae) {
                mainHandler.removeCallbacks(timeout);
                activeNetworkCallback = null;
                callback.onConnectionFailed("WiFi request failed: " + iae.getMessage());
            }
        }
    }
    
    /**
     * Legacy WiFi connection (Android 9 and below)
     */
    private void connectToWiFiLegacy(String ssid, String password, ConnectionCallback callback) {
        try {
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
        } catch (SecurityException se) {
            callback.onConnectionFailed("WiFi permission denied");
        } catch (Exception e) {
            callback.onConnectionFailed("WiFi connect error: " + e.getMessage());
        }
    }
    
    /**
     * Disconnect from current WiFi
     */
    public void disconnect() {
        if (connectivityManager != null) {
            try {
                if (activeNetworkCallback != null) {
                    connectivityManager.unregisterNetworkCallback(activeNetworkCallback);
                }
            } catch (Exception ignored) {
            }
            activeNetworkCallback = null;
            try {
                connectivityManager.bindProcessToNetwork(null);
            } catch (Exception ignored) {
            }
        }

        if (wifiManager != null) {
            try {
                wifiManager.disconnect();
            } catch (Exception ignored) {
            }
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
