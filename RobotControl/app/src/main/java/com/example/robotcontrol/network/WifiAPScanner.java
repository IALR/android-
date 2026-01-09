package com.example.robotcontrol.network;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * WiFi Access Point Scanner for detecting robot WiFi networks
 */
public class WifiAPScanner {
    
    private Context context;
    private WifiManager wifiManager;
    private List<ScanResult> scanResults;
    
    public interface ScanCallback {
        void onScanComplete(List<ScanResult> results);
        void onScanFailed(String error);
    }
    
    public WifiAPScanner(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        this.scanResults = new ArrayList<>();
    }
    
    /**
     * Start WiFi scan
     */
    public boolean startScan(ScanCallback callback) {
        if (wifiManager == null) {
            callback.onScanFailed("WiFi Manager not available");
            return false;
        }
        
        if (!wifiManager.isWifiEnabled()) {
            callback.onScanFailed("WiFi is disabled");
            return false;
        }
        
        // Check permissions for Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Permission check should be done in Activity before calling this
        }
        
        boolean success = wifiManager.startScan();
        if (success) {
            // Get scan results
            scanResults = wifiManager.getScanResults();
            callback.onScanComplete(scanResults);
        } else {
            callback.onScanFailed("Scan failed to start");
        }
        
        return success;
    }
    
    /**
     * Filter scan results to find robot APs
     * Assuming robot APs have specific SSID pattern like "ROBOT_*" or "ESP_*"
     */
    public List<ScanResult> filterRobotAPs(List<ScanResult> results) {
        List<ScanResult> robotAPs = new ArrayList<>();
        
        for (ScanResult result : results) {
            String ssid = result.SSID;
            if (ssid != null && (
                    ssid.equalsIgnoreCase("Robot_AP") ||
                    ssid.startsWith("ROBOT_") ||
                    ssid.startsWith("Robot") ||
                    ssid.startsWith("ESP_") ||
                    ssid.startsWith("Arduino_")
            )) {
                robotAPs.add(result);
            }
        }
        
        return robotAPs;
    }
    
    /**
     * Get signal strength level (0-4)
     */
    public int getSignalLevel(ScanResult result) {
        return WifiManager.calculateSignalLevel(result.level, 5);
    }
    
    /**
     * Check if WiFi is enabled
     */
    public boolean isWifiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }
    
    /**
     * Enable/Disable WiFi
     */
    public boolean setWifiEnabled(boolean enabled) {
        if (wifiManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android 10+, apps cannot enable/disable WiFi
                return false;
            }
            return wifiManager.setWifiEnabled(enabled);
        }
        return false;
    }
}
