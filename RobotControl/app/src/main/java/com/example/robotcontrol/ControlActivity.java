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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private TextView robotNameText, connectionStatusText;
    private TextView commandStatusText;
    private TextView receiveLogText;
    private ImageView connectionStatusIcon;
    private Button forwardButton, backwardButton, leftButton, rightButton, stopButton;
    private Button testButton;
    private SeekBar speedSeekBar, servo1SeekBar, servo2SeekBar;
    private TextView speedValue, servo1Value, servo2Value;

    private final SeekBar[] servoSeekBars = new SeekBar[8];
    private final TextView[] servoValueTexts = new TextView[8];
    private Toolbar toolbar;

    private String robotId;
    private String robotName;
    private Robot robot;
    private DatabaseHelper dbHelper;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean isConnected = false;

    private volatile boolean readLoopRunning = false;
    private Thread readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Get robot info from intent
        robotId = getIntent().getStringExtra("robot_id");
        robotName = getIntent().getStringExtra("robot_name");

        if (robotId == null || robotId.trim().isEmpty()) {
            Toast.makeText(this, "Missing robot id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        commandStatusText = findViewById(R.id.commandStatusText);
        receiveLogText = findViewById(R.id.receiveLogText);
        connectionStatusIcon = findViewById(R.id.connectionStatusIcon);

        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.backwardButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        stopButton = findViewById(R.id.stopButton);
        testButton = findViewById(R.id.testButton);

        speedSeekBar = findViewById(R.id.speedSlider);
        servo1SeekBar = findViewById(R.id.servoASlider);
        servo2SeekBar = findViewById(R.id.servoBSlider);

        speedValue = findViewById(R.id.speedValueText);
        servo1Value = findViewById(R.id.servoAValueText);
        servo2Value = findViewById(R.id.servoBValueText);

        robotNameText.setText(robotName);

        // Load robot details
        try {
            robot = dbHelper.getRobot(robotId);
        } catch (Exception e) {
            robot = null;
        }

        // Setup controls
        setupControls();
        enableControls(false);
        
        // Auto-connect on start
        connect();
    }

    private void setupControls() {
        forwardButton.setOnClickListener(v -> sendCommand("a"));
        backwardButton.setOnClickListener(v -> sendCommand("b"));
        leftButton.setOnClickListener(v -> sendCommand("l"));
        rightButton.setOnClickListener(v -> sendCommand("w"));
        stopButton.setOnClickListener(v -> sendCommand("s"));
        if (testButton != null) {
            testButton.setOnClickListener(v -> sendCommand("t"));
        }

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedValue.setText(String.valueOf(progress));
                // Not used by the single-letter servo robot protocol.
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 8-servo manual controls (Bluetooth command format: p<index>:<angle>; e.g. p3:120;)
        servoSeekBars[0] = servo1SeekBar;
        servoSeekBars[1] = servo2SeekBar;
        servoValueTexts[0] = servo1Value;
        servoValueTexts[1] = servo2Value;

        servoSeekBars[2] = findViewById(R.id.servoCSlider);
        servoSeekBars[3] = findViewById(R.id.servoDSlider);
        servoSeekBars[4] = findViewById(R.id.servoESlider);
        servoSeekBars[5] = findViewById(R.id.servoFSlider);
        servoSeekBars[6] = findViewById(R.id.servoGSlider);
        servoSeekBars[7] = findViewById(R.id.servoHSlider);

        servoValueTexts[2] = findViewById(R.id.servoCValueText);
        servoValueTexts[3] = findViewById(R.id.servoDValueText);
        servoValueTexts[4] = findViewById(R.id.servoEValueText);
        servoValueTexts[5] = findViewById(R.id.servoFValueText);
        servoValueTexts[6] = findViewById(R.id.servoGValueText);
        servoValueTexts[7] = findViewById(R.id.servoHValueText);

        for (int i = 0; i < 8; i++) {
            final int servoIndex = i + 1;
            SeekBar bar = servoSeekBars[i];
            TextView valueText = servoValueTexts[i];
            if (bar == null || valueText == null) continue;

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    valueText.setText(progress + "°");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    sendServoCommand(servoIndex, seekBar.getProgress());
                }
            });
        }
    }

    private void sendServoCommand(int servoIndex, int angle) {
        if (!isConnected || outputStream == null) {
            setCommandStatus("Servo " + servoIndex + ": not sent (not connected)");
            return;
        }
        if (servoIndex < 1 || servoIndex > 8) return;
        if (angle < 0) angle = 0;
        if (angle > 180) angle = 180;

        final String cmd = "p" + servoIndex + ":" + angle + ";";
        setCommandStatus("Servo " + servoIndex + ": sending " + angle + "°");

        int finalAngle = angle;
        new Thread(() -> {
            try {
                outputStream.write(cmd.getBytes());
                outputStream.flush();
                runOnUiThread(() -> setCommandStatus("Servo " + servoIndex + ": sent " + finalAngle + "°"));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to send servo command", Toast.LENGTH_SHORT).show();
                    isConnected = false;
                    updateConnectionStatus(false);
                    enableControls(false);
                    setCommandStatus("Servo " + servoIndex + ": failed");
                });
            }
        }).start();
    }

    private void connect() {
        if (robot == null) {
            Toast.makeText(this, "Robot not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            updateConnectionStatus(false);
            return;
        }

        if (robot.getMacAddress() == null || robot.getMacAddress().trim().isEmpty()) {
            Toast.makeText(this, "Robot Bluetooth address missing", Toast.LENGTH_SHORT).show();
            updateConnectionStatus(false);
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
                inputStream = bluetoothSocket.getInputStream();

                runOnUiThread(() -> {
                    isConnected = true;
                    updateConnectionStatus(true);
                    enableControls(true);
                    Toast.makeText(this, "Connected to " + robotName, Toast.LENGTH_SHORT).show();

                    setCommandStatus("Command: connected (ready)");
                    startReadLoop();
                    
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
                    setCommandStatus("Command: connect failed");
                });
            }
        }).start();
    }

    private void setCommandStatus(String text) {
        runOnUiThread(() -> {
            if (commandStatusText != null) {
                commandStatusText.setText(text);
            }
        });
    }

    private void appendReceiveLog(String line) {
        runOnUiThread(() -> {
            if (receiveLogText == null) return;
            String current = receiveLogText.getText() != null ? receiveLogText.getText().toString() : "";
            String updated = current.isEmpty() ? line : (current + "\n" + line);
            // Keep it short
            String[] parts = updated.split("\n");
            if (parts.length > 6) {
                StringBuilder sb = new StringBuilder();
                for (int i = parts.length - 6; i < parts.length; i++) {
                    sb.append(parts[i]);
                    if (i != parts.length - 1) sb.append("\n");
                }
                updated = sb.toString();
            }
            receiveLogText.setText(updated);
        });
    }

    private void startReadLoop() {
        if (inputStream == null) return;
        if (readLoopRunning) return;

        readLoopRunning = true;
        readThread = new Thread(() -> {
            StringBuilder lineBuffer = new StringBuilder();
            byte[] buf = new byte[256];
            while (readLoopRunning) {
                try {
                    int n = inputStream.read(buf);
                    if (n <= 0) continue;
                    for (int i = 0; i < n; i++) {
                        char c = (char) (buf[i] & 0xFF);
                        if (c == '\r') continue;
                        if (c == '\n') {
                            String line = lineBuffer.toString().trim();
                            lineBuffer.setLength(0);
                            if (!line.isEmpty()) {
                                appendReceiveLog("[Robot] " + line);
                            }
                        } else {
                            lineBuffer.append(c);
                        }
                    }
                } catch (IOException e) {
                    readLoopRunning = false;
                }
            }
        });
        readThread.start();
    }

    private void disconnect() {
        try {
            readLoopRunning = false;
            if (readThread != null) {
                try {
                    readThread.interrupt();
                } catch (Exception ignored) {
                }
                readThread = null;
            }

            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            isConnected = false;
            updateConnectionStatus(false);
            enableControls(false);

            setCommandStatus("Command: disconnected");
            
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
            setCommandStatus("Command: (not sent — not connected)");
            return;
        }

        if (command == null || command.trim().isEmpty()) return;
        final String cmd = command.trim().toLowerCase();
        setCommandStatus("Command: sending '" + cmd + "'");

        new Thread(() -> {
            try {
                // Match your Python serial behavior: send a single byte/character (no newline).
                outputStream.write(cmd.getBytes());
                outputStream.flush();
                runOnUiThread(() -> setCommandStatus("Command: sent '" + cmd + "'"));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show();
                    isConnected = false;
                    updateConnectionStatus(false);
                    enableControls(false);
                    setCommandStatus("Command: failed to send");
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
        for (int i = 0; i < servoSeekBars.length; i++) {
            if (servoSeekBars[i] != null) servoSeekBars[i].setEnabled(enable);
        }
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
