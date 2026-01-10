package com.example.robotcontrol;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RobotControlActivity extends AppCompatActivity {
    private RobotController robotController;
    private WiFiManagerHelper wifiManager;
    private WifiManager platformWifiManager;
    private TextView tvConnectionStatus;
    private TextView tvCommandStatus;
    private Button btnConnectToRobot, btnDisconnect, btnScanWiFi;
    private Spinner spinnerNetworks;
    private Button btnForward, btnBackward, btnLeft, btnRight, btnStop;
    private SeekBar sliderServo1, sliderServo2, sliderServo3;
    private TextView tvServo1Value, tvServo2Value, tvServo3Value;

    private static final String DEFAULT_ROBOT_SSID = "Robot_AP";
    private static final String ROBOT_PASSWORD = "12345678";
    private static final String DEFAULT_ROBOT_IP = "192.168.4.1";
    private static final int DEFAULT_ROBOT_PORT = 8888;

    private static final int REQ_WIFI_PERMS = 1201;

    private BroadcastReceiver wifiScanReceiver;
    private boolean scanReceiverRegistered = false;
    
    private String robotName = "Robot";
    private String robotIp = DEFAULT_ROBOT_IP;
    private int robotPort = DEFAULT_ROBOT_PORT;

    private String selectedSSID = DEFAULT_ROBOT_SSID;
    private List<String> availableNetworks = new ArrayList<>();
    private ArrayAdapter<String> networkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_control);

        // Read robot info passed from list/pairing screens
        if (getIntent() != null) {
            String extraName = getIntent().getStringExtra("robot_name");
            String extraIp = getIntent().getStringExtra("robot_ip");
            String extraSsid = getIntent().getStringExtra("robot_ssid");
            int extraPort = getIntent().getIntExtra("robot_port", DEFAULT_ROBOT_PORT);

            if (extraName != null && !extraName.trim().isEmpty()) {
                robotName = extraName.trim();
            }
            if (extraIp != null && !extraIp.trim().isEmpty()) {
                robotIp = extraIp.trim();
            }
            if (extraPort > 0) {
                robotPort = extraPort;
            }
            if (extraSsid != null && !extraSsid.trim().isEmpty()) {
                selectedSSID = extraSsid.trim();
            }
        }

        // Backward-compat: older saved WiFi robots stored BSSID (aa:bb:cc:dd:ee:ff) in the field.
        // If we detect a MAC-like value, fall back to the default SSID.
        selectedSSID = selectedSSID.replace("\"", "").trim();
        if (selectedSSID.matches("(?i)^([0-9a-f]{2}:){5}[0-9a-f]{2}$")) {
            selectedSSID = DEFAULT_ROBOT_SSID;
        }

        // Initialize managers
        robotController = new RobotController(robotIp, robotPort);
        wifiManager = new WiFiManagerHelper(this);
        platformWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        ensureWifiPermissions();

        // Initialize UI components
        initializeUI();

        // Set up button listeners
        setupButtonListeners();

        // Set up servo sliders
        setupServoSliders();

        updateConnectionStatus();
    }

    private void tryBindIfAlreadyConnected() {
        new Thread(() -> {
            try {
                if (wifiManager == null) return;
                boolean connected = wifiManager.isConnectedToNetwork(selectedSSID);
                if (connected) {
                    wifiManager.bindToCurrentWifiNetwork();
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private boolean hasWifiScanPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabledForWifiScan() {
        // On Android 9-12, Wi‑Fi scan results often require Location to be ON.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true;
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            return lm != null && lm.isLocationEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    private void ensureScanReceiver() {
        if (wifiScanReceiver != null) return;
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) return;

                List<ScanResult> scanResults;
                try {
                    scanResults = (platformWifiManager != null) ? platformWifiManager.getScanResults() : wifiManager.getAvailableNetworks();
                } catch (SecurityException se) {
                    scanResults = null;
                }

                Set<String> unique = new LinkedHashSet<>();
                if (scanResults != null) {
                    for (ScanResult result : scanResults) {
                        if (result == null) continue;
                        String ssid = result.SSID;
                        if (ssid == null) continue;
                        ssid = ssid.trim();
                        if (ssid.isEmpty()) continue;
                        unique.add(ssid);
                    }
                }

                availableNetworks.clear();
                availableNetworks.addAll(unique);

                runOnUiThread(() -> {
                    networkAdapter.notifyDataSetChanged();
                    if (availableNetworks.isEmpty()) {
                        Toast.makeText(RobotControlActivity.this, "No networks found. Check WiFi permission + Location ON.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RobotControlActivity.this, "Found " + availableNetworks.size() + " networks", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    private void ensureWifiPermissions() {
        // targetSdk=36: Android 13+ requires NEARBY_WIFI_DEVICES for scanning.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, REQ_WIFI_PERMS);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_WIFI_PERMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WIFI_PERMS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                Toast.makeText(this, "WiFi permission is required to scan/connect", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeUI() {
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvCommandStatus = findViewById(R.id.tvCommandStatus);
        btnConnectToRobot = findViewById(R.id.btnConnectToRobot);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnScanWiFi = findViewById(R.id.btnScanWiFi);
        spinnerNetworks = findViewById(R.id.spinnerNetworks);

        // Motor buttons
        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnStop = findViewById(R.id.btnStop);

        // Servo sliders
        sliderServo1 = findViewById(R.id.sliderServo1);
        sliderServo2 = findViewById(R.id.sliderServo2);
        sliderServo3 = findViewById(R.id.sliderServo3);

        // Servo value displays
        tvServo1Value = findViewById(R.id.tvServo1Value);
        tvServo2Value = findViewById(R.id.tvServo2Value);
        tvServo3Value = findViewById(R.id.tvServo3Value);
        
        // Setup network spinner
        networkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableNetworks);
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNetworks.setAdapter(networkAdapter);

        if (tvCommandStatus != null) {
            tvCommandStatus.setText("Command: (none yet)");
        }
    }

    private void setCommandStatus(String text) {
        runOnUiThread(() -> {
            if (tvCommandStatus != null) {
                tvCommandStatus.setText(text);
            }
        });
    }

    private boolean ensureReadyToSend(String actionLabel) {
        try {
            boolean connected = wifiManager != null && wifiManager.isConnectedToNetwork(selectedSSID);
            if (!connected) {
                setCommandStatus("Command: " + actionLabel + " (NOT sent — not connected to " + selectedSSID + ")");
                Toast.makeText(this, "Not connected to robot WiFi (" + selectedSSID + ")", Toast.LENGTH_SHORT).show();
                return false;
            }
            // Best-effort bind to the Wi‑Fi transport so HTTP actually goes to ESP32.
            new Thread(() -> {
                try {
                    if (wifiManager != null) wifiManager.bindToCurrentWifiNetwork();
                } catch (Exception ignored) {
                }
            }).start();
            return true;
        } catch (Exception e) {
            setCommandStatus("Command: " + actionLabel + " (error: " + e.getMessage() + ")");
            return false;
        }
    }

    private void setupButtonListeners() {
        btnScanWiFi.setOnClickListener(v -> scanForRobots());
        btnConnectToRobot.setOnClickListener(v -> connectToSelectedRobot());
        btnDisconnect.setOnClickListener(v -> disconnectFromRobot());

        // Motor controls with press/release
        btnForward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (ensureReadyToSend("forward")) {
                        robotController.moveForward();
                    }
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (ensureReadyToSend("stop")) {
                        robotController.stopMotors();
                    }
                    break;
            }
            return true;
        });

        btnBackward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (ensureReadyToSend("backward")) {
                        robotController.moveBackward();
                    }
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (ensureReadyToSend("stop")) {
                        robotController.stopMotors();
                    }
                    break;
            }
            return true;
        });

        btnLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (ensureReadyToSend("left")) {
                        robotController.turnLeft();
                    }
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (ensureReadyToSend("stop")) {
                        robotController.stopMotors();
                    }
                    break;
            }
            return true;
        });

        btnRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (ensureReadyToSend("right")) {
                        robotController.turnRight();
                    }
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (ensureReadyToSend("stop")) {
                        robotController.stopMotors();
                    }
                    break;
            }
            return true;
        });

        btnStop.setOnClickListener(v -> {
            if (ensureReadyToSend("stop")) {
                robotController.stopMotors();
            }
        });
            robotController.setCommandCallback(new RobotController.CommandCallback() {
                @Override
                public void onSending(String endpoint, String url) {
                    setCommandStatus("Command: " + endpoint + " (sending...)");
                }

                @Override
                public void onResult(String endpoint, int httpCode, String body) {
                    String shortBody = body;
                    if (shortBody == null) shortBody = "";
                    shortBody = shortBody.trim();
                    if (shortBody.length() > 120) {
                        shortBody = shortBody.substring(0, 120) + "...";
                    }
                    setCommandStatus("Command: " + endpoint + " (HTTP " + httpCode + ") " + shortBody);
                }

                @Override
                public void onError(String endpoint, Exception error) {
                    String msg = (error != null && error.getMessage() != null) ? error.getMessage() : "Unknown error";
                    setCommandStatus("Command: " + endpoint + " (FAILED: " + msg + ")");
                }
            });
    }

    private void setupServoSliders() {
        sliderServo1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    robotController.setBaseServo(progress);
                    tvServo1Value.setText(progress + "°");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sliderServo2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    robotController.setShoulderServo(progress);
                    tvServo2Value.setText(progress + "°");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sliderServo3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    robotController.setGripperServo(progress);
                    tvServo3Value.setText(progress + "°");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void scanForRobots() {
        ensureWifiPermissions();

        if (!hasWifiScanPermission()) {
            Toast.makeText(this, "Grant WiFi permission first", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isLocationEnabledForWifiScan()) {
            Toast.makeText(this, "Turn ON Location (required for WiFi scan)", Toast.LENGTH_LONG).show();
            return;
        }

        wifiManager.enableWiFi();
        Toast.makeText(this, "Scanning for WiFi networks...", Toast.LENGTH_SHORT).show();

        ensureScanReceiver();
        if (!scanReceiverRegistered) {
            registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            scanReceiverRegistered = true;
        }

        boolean started = false;
        try {
            if (platformWifiManager != null) {
                started = platformWifiManager.startScan();
            }
        } catch (SecurityException ignored) {
        }

        if (!started) {
            // Some devices throttle scans; show best-effort cached results.
            List<ScanResult> scanResults = null;
            try {
                scanResults = (platformWifiManager != null) ? platformWifiManager.getScanResults() : wifiManager.getAvailableNetworks();
            } catch (SecurityException ignored) {
            }

            Set<String> unique = new LinkedHashSet<>();
            if (scanResults != null) {
                for (ScanResult result : scanResults) {
                    if (result == null) continue;
                    String ssid = result.SSID;
                    if (ssid == null) continue;
                    ssid = ssid.trim();
                    if (ssid.isEmpty()) continue;
                    unique.add(ssid);
                }
            }

            availableNetworks.clear();
            availableNetworks.addAll(unique);
            networkAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Scan throttled. Showing last results (" + availableNetworks.size() + ")", Toast.LENGTH_LONG).show();
        }
    }

    private void connectToSelectedRobot() {
        ensureWifiPermissions();
        if (spinnerNetworks.getSelectedItem() == null) {
            Toast.makeText(this, "Please scan and select a network first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        selectedSSID = spinnerNetworks.getSelectedItem().toString();
        Toast.makeText(this, "Connecting to " + selectedSSID + "...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            boolean connected = wifiManager.connectToNetwork(selectedSSID, ROBOT_PASSWORD);

            runOnUiThread(() -> {
                if (connected) {
                    Toast.makeText(RobotControlActivity.this, "Connected to " + selectedSSID + " (allow the system prompt if shown)", Toast.LENGTH_SHORT).show();
                    updateConnectionStatus();
                } else {
                    Toast.makeText(RobotControlActivity.this, "Failed to connect. Make sure you accepted the WiFi connect prompt.", Toast.LENGTH_LONG).show();
                    updateConnectionStatus();
                }
            });
        }).start();
    }

    private void disconnectFromRobot() {
        robotController.stopMotors();
        wifiManager.disconnect();
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        updateConnectionStatus();
    }

    private void updateConnectionStatus() {
        new Thread(() -> {
            try {
                boolean isConnected = wifiManager.isConnectedToNetwork(selectedSSID);
                runOnUiThread(() -> {
                    if (isConnected) {
                        tvConnectionStatus.setText("Status: ✓ Connected to " + selectedSSID);
                        tvConnectionStatus.setTextColor(getColor(R.color.status_connected));
                        btnConnectToRobot.setEnabled(false);
                        btnDisconnect.setEnabled(true);
                    } else {
                        tvConnectionStatus.setText("Status: ✗ Disconnected");
                        tvConnectionStatus.setTextColor(getColor(R.color.status_disconnected));
                        btnConnectToRobot.setEnabled(true);
                        btnDisconnect.setEnabled(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectionStatus();
        tryBindIfAlreadyConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanReceiverRegistered) {
            try {
                unregisterReceiver(wifiScanReceiver);
            } catch (Exception ignored) {
            }
            scanReceiverRegistered = false;
        }
    }
}
