package com.example.robotcontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.adapters.DeviceAdapter;
import com.example.robotcontrol.adapters.WifiNetworkAdapter;
import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;
import com.example.robotcontrol.network.WiFiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PairingActivity extends AppCompatActivity implements WifiNetworkAdapter.OnNetworkClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private static final String ROBOT_DEFAULT_PASSWORD = "12345678";
    private static final String ROBOT_DEFAULT_IP = "192.168.4.1";

    private RecyclerView devicesRecyclerView;
    private WifiNetworkAdapter networkAdapter;
    private List<ScanResult> networkList;
    private Button scanButton;
    private Button scanWifiButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private Toolbar toolbar;

    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter bluetoothDeviceAdapter;
    private List<BluetoothDevice> bluetoothDeviceList;

    private WifiManager platformWifiManager;
    private WiFiManager wifiConnector;
    private DatabaseHelper dbHelper;
    private String currentUserId;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        // Get current user ID from intent or use default
        currentUserId = getIntent().getStringExtra("user_id");
        if (currentUserId == null) {
            currentUserId = "local_user";
        }

        dbHelper = new DatabaseHelper(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        platformWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConnector = new WiFiManager(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pair_robot);
        }

        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
        scanButton = findViewById(R.id.scanButton);
        scanWifiButton = findViewById(R.id.scanWifiButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        // Setup RecyclerView (default = Bluetooth list)
        bluetoothDeviceList = new ArrayList<>();
        bluetoothDeviceAdapter = new DeviceAdapter(this, bluetoothDeviceList, device -> onBluetoothDeviceClick(device));

        networkList = new ArrayList<>();
        networkAdapter = new WifiNetworkAdapter(networkList, this);

        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesRecyclerView.setAdapter(bluetoothDeviceAdapter);

        scanButton.setOnClickListener(v -> startBluetoothScanning());
        scanWifiButton.setOnClickListener(v -> startWifiScanning());

        // Initialize the broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && !bluetoothDeviceList.contains(device)) {
                        bluetoothDeviceList.add(device);
                        bluetoothDeviceAdapter.notifyDataSetChanged();
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    progressBar.setVisibility(View.GONE);
                    scanButton.setEnabled(true);
                    statusText.setText(R.string.scan_complete);
                } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    progressBar.setVisibility(View.GONE);
                    scanWifiButton.setEnabled(true);

                    List<ScanResult> results;
                    try {
                        results = platformWifiManager != null ? platformWifiManager.getScanResults() : null;
                    } catch (SecurityException e) {
                        results = null;
                    }

                    networkList.clear();
                    if (results != null) {
                        for (ScanResult r : results) {
                            if (r == null || r.SSID == null) continue;
                            String ssid = r.SSID.trim();
                            if (ssid.isEmpty()) continue;
                            // Prefer robot SSIDs; keep list minimal and relevant
                            if (ssid.equalsIgnoreCase("Robot_AP") || ssid.startsWith("Robot") || ssid.startsWith("ROBOT_") || ssid.startsWith("ESP_")) {
                                networkList.add(r);
                            }
                        }
                    }

                    networkAdapter.notifyDataSetChanged();

                    if (networkList.isEmpty()) {
                        statusText.setText("No robot WiFi found. Turn ON WiFi + Location, then scan again.");
                    } else {
                        statusText.setText("Found " + networkList.size() + " robot WiFi networks");
                    }
                }
            }
        };

        // Check permissions
        checkPermissions();

        // Register for WiFi scan results
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        // Register for Bluetooth discovery
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // WiFi scan permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        // Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "WiFi permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startBluetoothScanning() {
        checkPermissions();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } catch (Exception e) {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Show Bluetooth list
        devicesRecyclerView.setAdapter(bluetoothDeviceAdapter);
        bluetoothDeviceList.clear();
        bluetoothDeviceAdapter.notifyDataSetChanged();

        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            progressBar.setVisibility(View.VISIBLE);
            statusText.setText(R.string.scanning_bluetooth);
            scanButton.setEnabled(false);

            bluetoothAdapter.startDiscovery();
        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            scanButton.setEnabled(true);
        }
    }

    private void startWifiScanning() {
        checkPermissions();

        if (platformWifiManager == null) {
            Toast.makeText(this, "WiFi not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!platformWifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Turn ON WiFi, then scan again", Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Android 9-12 commonly require Location ON for WiFi scan results
            try {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (lm != null && !lm.isLocationEnabled()) {
                    Toast.makeText(this, "Turn ON Location (required for WiFi scanning)", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // Show WiFi list
        devicesRecyclerView.setAdapter(networkAdapter);
        networkList.clear();
        networkAdapter.notifyDataSetChanged();

        try {
            progressBar.setVisibility(View.VISIBLE);
            statusText.setText(R.string.scanning_wifi);
            scanWifiButton.setEnabled(false);

            boolean started = platformWifiManager.startScan();
            if (!started) {
                progressBar.setVisibility(View.GONE);
                scanWifiButton.setEnabled(true);
                statusText.setText("Scan throttled. Try again in a few seconds.");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "WiFi permission denied", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            scanWifiButton.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startBluetoothScanning();
            } else {
                Toast.makeText(this, "Bluetooth must be enabled to scan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onBluetoothDeviceClick(BluetoothDevice device) {
        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        } catch (Exception ignored) {
        }
        showAddRobotDialogBluetooth(device);
    }

    @Override
    public void onNetworkClick(ScanResult network) {
        try {
            if (network == null || network.SSID == null || network.SSID.trim().isEmpty()) {
                Toast.makeText(this, "Invalid network", Toast.LENGTH_SHORT).show();
                return;
            }

            String ssid = network.SSID.trim();
            // Even if the phone is already connected, we still request/bind the network here.
            // On Android 10+ the system may keep routing via cellular unless the app binds to Wi‑Fi.

            statusText.setText("Connecting to " + ssid + "... (accept the Android prompt)");
            progressBar.setVisibility(View.VISIBLE);
            scanButton.setEnabled(false);
            scanWifiButton.setEnabled(false);

            wifiConnector.connectToWiFi(ssid, ROBOT_DEFAULT_PASSWORD, new WiFiManager.ConnectionCallback() {
                @Override
                public void onConnected(Network net) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        scanButton.setEnabled(true);
                        scanWifiButton.setEnabled(true);
                        statusText.setText("Connected to " + ssid);
                        showAddRobotDialog(ssid, network.BSSID);
                    });
                }

                @Override
                public void onConnectionFailed(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        scanButton.setEnabled(true);
                        scanWifiButton.setEnabled(true);
                        statusText.setText("Connection failed");
                        Toast.makeText(PairingActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onDisconnected() {
                    runOnUiThread(() -> statusText.setText("Disconnected"));
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            scanButton.setEnabled(true);
            scanWifiButton.setEnabled(true);
            statusText.setText("Connection error");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddRobotDialogBluetooth(BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_robot, null);

        EditText nameInput = dialogView.findViewById(R.id.robotNameInput);
        EditText typeInput = dialogView.findViewById(R.id.robotTypeInput);

        String deviceName = "Unknown Device";
        try {
            if (device != null && device.getName() != null) {
                deviceName = device.getName();
            }
        } catch (SecurityException ignored) {
        }
        nameInput.setText(deviceName);

        final String finalDeviceName = deviceName;

        builder.setView(dialogView)
                .setTitle(R.string.add_robot)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String type = typeInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        name = finalDeviceName;
                    }
                    if (type.isEmpty()) {
                        type = "Generic Robot";
                    }

                    addRobotBluetooth(device, name, type);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addRobotBluetooth(BluetoothDevice device, String name, String type) {
        String robotId = UUID.randomUUID().toString();
        String macAddress = (device != null) ? device.getAddress() : "";

        Robot robot = new Robot(robotId, name, macAddress, "", type, currentUserId);
        robot.setConnectionType("bluetooth");

        long result = dbHelper.addRobot(robot);

        if (result != -1) {
            Toast.makeText(this, "Robot added successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to add robot", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddRobotDialog(String ssid, String bssid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_robot, null);
        
        EditText nameInput = dialogView.findViewById(R.id.robotNameInput);
        EditText typeInput = dialogView.findViewById(R.id.robotTypeInput);

        nameInput.setText(ssid);
        final String finalSsid = ssid;
        final String finalBssid = bssid;

        builder.setView(dialogView)
                .setTitle(R.string.add_robot)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String type = typeInput.getText().toString().trim();
                    
                    if (name.isEmpty()) {
                        name = finalSsid;
                    }
                    if (type.isEmpty()) {
                        type = "Generic Robot";
                    }

                    addRobot(finalSsid, finalBssid, name, type);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addRobot(String ssid, String bssid, String name, String type) {
        String robotId = UUID.randomUUID().toString();

        // For WiFi robots, we store SSID in macAddress field (legacy name) so we can later reconnect.
        String macAddress = ssid;
        Robot robot = new Robot(robotId, name, macAddress, ROBOT_DEFAULT_IP, type, currentUserId);
        robot.setConnectionType("wifi");

        // Save to local database only
        long result = dbHelper.addRobot(robot);
        
        if (result != -1) {
            Toast.makeText(this, "Robot added successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to add robot", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
