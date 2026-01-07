package com.example.robotcontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private TextView robotNameText, connectionStatusText;
    private ImageView connectionStatusIcon;
    private Button forwardButton, backwardButton, leftButton, rightButton, stopButton;
    private SeekBar speedSeekBar, servo1SeekBar, servo2SeekBar;
    private TextView speedValue, servo1Value, servo2Value;
    private Toolbar toolbar;

    private String robotId;
    private String robotName;
    private Robot robot;
    private DatabaseHelper dbHelper;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Get robot info from intent
        robotId = getIntent().getStringExtra("robot_id");
        robotName = getIntent().getStringExtra("robot_name");

        dbHelper = new DatabaseHelper(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(robotName);
        }

        robotNameText = findViewById(R.id.robotNameText);
        connectionStatusText = findViewById(R.id.connectionStatusText);
        connectionStatusIcon = findViewById(R.id.connectionStatusIcon);

        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.backwardButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        stopButton = findViewById(R.id.stopButton);

        speedSeekBar = findViewById(R.id.speedSlider);
        servo1SeekBar = findViewById(R.id.servoASlider);
        servo2SeekBar = findViewById(R.id.servoBSlider);

        speedValue = findViewById(R.id.speedValueText);
        servo1Value = findViewById(R.id.servoAValueText);
        servo2Value = findViewById(R.id.servoBValueText);

        robotNameText.setText(robotName);

        // Load robot details
        robot = dbHelper.getRobot(robotId);

        // Setup controls
        setupControls();
        enableControls(false);
        
        // Auto-connect on start
        connect();
    }

    private void setupControls() {
        forwardButton.setOnClickListener(v -> sendCommand("F"));
        backwardButton.setOnClickListener(v -> sendCommand("B"));
        leftButton.setOnClickListener(v -> sendCommand("L"));
        rightButton.setOnClickListener(v -> sendCommand("R"));
        stopButton.setOnClickListener(v -> sendCommand("S"));

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedValue.setText(String.valueOf(progress));
                if (fromUser && isConnected) {
                    sendCommand("V" + progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        servo1SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                servo1Value.setText(progress + "°");
                if (fromUser && isConnected) {
                    sendCommand("A" + progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        servo2SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                servo2Value.setText(progress + "°");
                if (fromUser && isConnected) {
                    sendCommand("B" + progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void connect() {
        if (robot == null) {
            Toast.makeText(this, "Robot not found", Toast.LENGTH_SHORT).show();
            return;
        }

        connectionStatusText.setText(R.string.connecting);

        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show();
                        updateConnectionStatus(false);
                    });
                    return;
                }

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(robot.getMacAddress());
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                runOnUiThread(() -> {
                    isConnected = true;
                    updateConnectionStatus(true);
                    enableControls(true);
                    Toast.makeText(this, "Connected to " + robotName, Toast.LENGTH_SHORT).show();
                    
                    // Update last connected time in local database
                    robot.setLastConnected(System.currentTimeMillis());
                    robot.setConnected(true);
                    dbHelper.updateRobot(robot);
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Connection failed: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    updateConnectionStatus(false);
                });
            }
        }).start();
    }

    private void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            isConnected = false;
            updateConnectionStatus(false);
            enableControls(false);
            
            if (robot != null) {
                robot.setConnected(false);
                dbHelper.updateRobot(robot);
            }
            
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error disconnecting: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCommand(String command) {
        if (!isConnected || outputStream == null) {
            Toast.makeText(this, "Not connected to robot", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                outputStream.write((command + "\n").getBytes());
                outputStream.flush();
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show();
                    isConnected = false;
                    updateConnectionStatus(false);
                    enableControls(false);
                });
            }
        }).start();
    }

    private void updateConnectionStatus(boolean connected) {
        isConnected = connected;
        if (connected) {
            connectionStatusText.setText(R.string.connected);
            connectionStatusText.setTextColor(getResources().getColor(R.color.status_connected));
            connectionStatusIcon.setImageResource(R.drawable.ic_connected);
        } else {
            connectionStatusText.setText(R.string.disconnected);
            connectionStatusText.setTextColor(getResources().getColor(R.color.status_disconnected));
            connectionStatusIcon.setImageResource(R.drawable.ic_disconnected);
        }
    }

    private void enableControls(boolean enable) {
        forwardButton.setEnabled(enable);
        backwardButton.setEnabled(enable);
        leftButton.setEnabled(enable);
        rightButton.setEnabled(enable);
        stopButton.setEnabled(enable);
        speedSeekBar.setEnabled(enable);
        servo1SeekBar.setEnabled(enable);
        servo2SeekBar.setEnabled(enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected) {
            disconnect();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
