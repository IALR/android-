package com.example.robotcontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PairingActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private RecyclerView devicesRecyclerView;
    private DeviceAdapter deviceAdapter;
    private List<BluetoothDevice> deviceList;
    private Button scanButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private Toolbar toolbar;

    private BluetoothAdapter bluetoothAdapter;
    private DatabaseHelper dbHelper;
    private String currentUserId;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        // Get current user ID from intent or use default
        currentUserId = getIntent().getStringExtra("user_id");
        if (currentUserId == null) {
            currentUserId = "local_user";
        }

        dbHelper = new DatabaseHelper(this);

        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pair_robot);
        }

        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
        scanButton = findViewById(R.id.scanButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        // Setup RecyclerView
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList, this);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesRecyclerView.setAdapter(deviceAdapter);

        scanButton.setOnClickListener(v -> startScanning());

        // Initialize the broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && !deviceList.contains(device)) {
                        deviceList.add(device);
                        deviceAdapter.notifyDataSetChanged();
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(R.string.scan_complete);
                    scanButton.setEnabled(true);
                }
            }
        };

        // Check permissions
        checkPermissions();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Register for broadcasts when discovery has finished
        IntentFilter finishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, finishedFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                        == PackageManager.PERMISSION_GRANTED) {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            // Below Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
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
                Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startScanning() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } catch (SecurityException e) {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();

        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            progressBar.setVisibility(View.VISIBLE);
            statusText.setText(R.string.scanning);
            scanButton.setEnabled(false);

            bluetoothAdapter.startDiscovery();
        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            scanButton.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScanning();
            } else {
                Toast.makeText(this, "Bluetooth must be enabled to scan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDeviceClick(BluetoothDevice device) {
        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        showAddRobotDialog(device);
    }

    private void showAddRobotDialog(BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_robot, null);
        
        EditText nameInput = dialogView.findViewById(R.id.robotNameInput);
        EditText typeInput = dialogView.findViewById(R.id.robotTypeInput);
        
        String deviceName = "Unknown Device";
        try {
            if (device.getName() != null) {
                deviceName = device.getName();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
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
                    
                    addRobot(device, name, type);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addRobot(BluetoothDevice device, String name, String type) {
        String robotId = UUID.randomUUID().toString();
        String macAddress = device.getAddress();
        
        Robot robot = new Robot(robotId, name, macAddress, "", type, currentUserId);
        robot.setConnectionType("bluetooth");

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
