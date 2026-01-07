package com.example.robotcontrol.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectionManager {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int RECONNECT_DELAY = 5000; // 5 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    private Context context;
    private DatabaseHelper dbHelper;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean isConnected = false;
    private boolean autoReconnect = true;
    private int reconnectAttempts = 0;
    private Handler reconnectHandler;

    private ConnectionListener connectionListener;
    private DataListener dataListener;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(String error);
    }

    public interface DataListener {
        void onDataReceived(String data);
    }

    public ConnectionManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.reconnectHandler = new Handler(Looper.getMainLooper());
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect(Robot robot) {
        if (isConnected) {
            return;
        }

        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    notifyConnectionFailed("Bluetooth permission not granted");
                    return;
                }

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(robot.getMacAddress());
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                
                isConnected = true;
                reconnectAttempts = 0;
                
                // Update robot status
                robot.setConnected(true);
                robot.setLastConnected(System.currentTimeMillis());
                dbHelper.updateRobot(robot);
                
                notifyConnected();
                startListening();
                
            } catch (IOException e) {
                isConnected = false;
                notifyConnectionFailed(e.getMessage());
                
                if (autoReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    scheduleReconnect(robot);
                }
            }
        }).start();
    }

    private void scheduleReconnect(Robot robot) {
        reconnectAttempts++;
        reconnectHandler.postDelayed(() -> connect(robot), RECONNECT_DELAY);
    }

    public void disconnect() {
        autoReconnect = false;
        reconnectHandler.removeCallbacksAndMessages(null);
        
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            isConnected = false;
            notifyDisconnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command) {
        if (!isConnected || outputStream == null) {
            return;
        }

        new Thread(() -> {
            try {
                outputStream.write((command + "\n").getBytes());
                outputStream.flush();
            } catch (IOException e) {
                isConnected = false;
                notifyDisconnected();
            }
        }).start();
    }

    private void startListening() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (isConnected) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String data = new String(buffer, 0, bytes);
                        notifyDataReceived(data);
                    }
                } catch (IOException e) {
                    isConnected = false;
                    notifyDisconnected();
                    break;
                }
            }
        }).start();
    }

    private void notifyConnected() {
        if (connectionListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> connectionListener.onConnected());
        }
    }

    private void notifyDisconnected() {
        if (connectionListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> connectionListener.onDisconnected());
        }
    }

    private void notifyConnectionFailed(String error) {
        if (connectionListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> connectionListener.onConnectionFailed(error));
        }
    }

    private void notifyDataReceived(String data) {
        if (dataListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> dataListener.onDataReceived(data));
        }
    }
}
