package com.example.robotcontrol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WiFiManagerHelper {
    private Context context;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback activeNetworkCallback;
    private Network boundNetwork;

    public WiFiManagerHelper(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isWiFiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    public void enableWiFi() {
        if (wifiManager == null) return;
        if (!isWiFiEnabled()) {
            // On Android 10+ this may be ignored; user may need to enable Wi‑Fi manually.
            wifiManager.setWifiEnabled(true);
        }
    }

    public void scanNetworks() {
        if (wifiManager != null) {
            wifiManager.startScan();
        }
    }

    public List<ScanResult> getAvailableNetworks() {
        if (wifiManager != null) {
            return wifiManager.getScanResults();
        }
        return null;
    }

    public boolean connectToNetwork(String ssid, String password) {
        if (wifiManager == null || connectivityManager == null) return false;

        // Android 10+ recommended approach: WifiNetworkSpecifier + ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            disconnect();

            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build();

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier)
                    .build();

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = new boolean[] { false };

            activeNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    boundNetwork = network;
                    connectivityManager.bindProcessToNetwork(network);
                    success[0] = true;
                    latch.countDown();
                }

                @Override
                public void onUnavailable() {
                    success[0] = false;
                    latch.countDown();
                }

                @Override
                public void onLost(Network network) {
                    if (boundNetwork != null && boundNetwork.equals(network)) {
                        connectivityManager.bindProcessToNetwork(null);
                        boundNetwork = null;
                    }
                }
            };

            // This will show a system prompt to the user to allow the connection.
            connectivityManager.requestNetwork(request, activeNetworkCallback);

            try {
                latch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return false;
            }

            return success[0];
        }

        // Min SDK in this project is 29, so this path should not be hit.
        return false;
    }

    /**
     * Bind this app's process to any currently available Wi‑Fi network.
     * Useful when the device is already connected to the robot AP via Settings, but traffic still
     * routes over cellular because the Wi‑Fi has no internet.
     */
    public boolean bindToCurrentWifiNetwork() {
        if (connectivityManager == null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false;

        // Unbind/unregister any previous callback (do NOT disconnect Wi‑Fi here).
        try {
            if (activeNetworkCallback != null) {
                connectivityManager.unregisterNetworkCallback(activeNetworkCallback);
            }
        } catch (Exception ignored) {
        }
        activeNetworkCallback = null;

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[] { false };

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        activeNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                boundNetwork = network;
                connectivityManager.bindProcessToNetwork(network);
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onUnavailable() {
                success[0] = false;
                latch.countDown();
            }

            @Override
            public void onLost(Network network) {
                if (boundNetwork != null && boundNetwork.equals(network)) {
                    connectivityManager.bindProcessToNetwork(null);
                    boundNetwork = null;
                }
            }
        };

        try {
            connectivityManager.requestNetwork(request, activeNetworkCallback);
        } catch (Exception e) {
            return false;
        }

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return false;
        }

        return success[0];
    }

    public void disconnect() {
        if (connectivityManager != null) {
            try {
                if (activeNetworkCallback != null) {
                    connectivityManager.unregisterNetworkCallback(activeNetworkCallback);
                }
            } catch (Exception ignored) {
                // Ignored: callback may already be unregistered.
            }
            activeNetworkCallback = null;
            connectivityManager.bindProcessToNetwork(null);
            boundNetwork = null;
        }

        if (wifiManager != null) {
            try {
                wifiManager.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    public String getConnectedSSID() {
        if (wifiManager == null) return "";
        try {
            String ssid = wifiManager.getConnectionInfo().getSSID();
            if (ssid == null) return "";
            return ssid.replace("\"", "");
        } catch (SecurityException e) {
            return "";
        }
    }

    public boolean isConnectedToNetwork(String ssid) {
        try {
            String connectedSSID = getConnectedSSID();
            return connectedSSID != null && connectedSSID.equals(ssid);
        } catch (Exception e) {
            return false;
        }
    }
}
