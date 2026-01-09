package com.example.robotcontrol.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * Background service for auto-reconnecting to robot
 * Maintains connection and attempts reconnection on disconnect
 */
public class AutoReconnectService extends Service {
    
    private static final String TAG = "AutoReconnectService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int RECONNECT_DELAY_MS = 5000; // 5 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice connectedDevice;
    private Handler reconnectHandler;
    private Runnable reconnectRunnable;
    
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    private int reconnectAttempts = 0;
    
    private final IBinder binder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        public AutoReconnectService getService() {
            return AutoReconnectService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        reconnectHandler = new Handler();
        
        reconnectRunnable = new Runnable() {
            @Override
            public void run() {
                if (shouldReconnect && !isConnected && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++;
                    Log.d(TAG, "Attempting reconnection... Attempt: " + reconnectAttempts);
                    attemptReconnect();
                    
                    // Schedule next attempt
                    reconnectHandler.postDelayed(this, RECONNECT_DELAY_MS);
                } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    Log.d(TAG, "Max reconnect attempts reached");
                    notifyConnectionFailed();
                }
            }
        };
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("CONNECT".equals(action)) {
                String deviceAddress = intent.getStringExtra("DEVICE_ADDRESS");
                if (deviceAddress != null) {
                    connectToDevice(deviceAddress);
                }
            } else if ("DISCONNECT".equals(action)) {
                disconnect();
            }
        }
        return START_STICKY; // Service will be restarted if killed
    }
    
    /**
     * Connect to Bluetooth device
     */
    public void connectToDevice(String deviceAddress) {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter not available");
            return;
        }
        
        connectedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        
        new Thread(() -> {
            try {
                bluetoothSocket = connectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                
                isConnected = true;
                reconnectAttempts = 0;
                shouldReconnect = true;
                
                Log.d(TAG, "Connected to device: " + deviceAddress);
                notifyConnectionSuccess();
                
                // Monitor connection
                monitorConnection();
                
            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                isConnected = false;
                
                // Start reconnection attempts
                if (shouldReconnect) {
                    reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_MS);
                }
            }
        }).start();
    }
    
    /**
     * Attempt to reconnect
     */
    private void attemptReconnect() {
        if (connectedDevice != null) {
            try {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    bluetoothSocket.close();
                }
                
                bluetoothSocket = connectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                
                isConnected = true;
                reconnectAttempts = 0;
                
                Log.d(TAG, "Reconnection successful");
                notifyReconnectionSuccess();
                
                // Continue monitoring
                monitorConnection();
                
            } catch (IOException e) {
                Log.e(TAG, "Reconnection failed: " + e.getMessage());
                isConnected = false;
            }
        }
    }
    
    /**
     * Monitor connection status
     */
    private void monitorConnection() {
        new Thread(() -> {
            while (isConnected && bluetoothSocket != null) {
                try {
                    if (!bluetoothSocket.isConnected()) {
                        Log.d(TAG, "Connection lost");
                        isConnected = false;
                        notifyConnectionLost();
                        
                        // Start reconnection
                        if (shouldReconnect) {
                            reconnectHandler.post(reconnectRunnable);
                        }
                        break;
                    }
                    
                    Thread.sleep(1000); // Check every second
                    
                } catch (InterruptedException e) {
                    Log.e(TAG, "Monitoring interrupted: " + e.getMessage());
                    break;
                }
            }
        }).start();
    }
    
    /**
     * Disconnect from device
     */
    public void disconnect() {
        shouldReconnect = false;
        reconnectHandler.removeCallbacks(reconnectRunnable);
        
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            isConnected = false;
            Log.d(TAG, "Disconnected from device");
            
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Send broadcast notifications
     */
    private void notifyConnectionSuccess() {
        Intent intent = new Intent("com.example.robotcontrol.CONNECTION_SUCCESS");
        sendBroadcast(intent);
    }
    
    private void notifyReconnectionSuccess() {
        Intent intent = new Intent("com.example.robotcontrol.RECONNECTION_SUCCESS");
        sendBroadcast(intent);
    }
    
    private void notifyConnectionLost() {
        Intent intent = new Intent("com.example.robotcontrol.CONNECTION_LOST");
        sendBroadcast(intent);
    }
    
    private void notifyConnectionFailed() {
        Intent intent = new Intent("com.example.robotcontrol.CONNECTION_FAILED");
        sendBroadcast(intent);
    }
    
    /**
     * Check connection status
     */
    public boolean isConnected() {
        return isConnected && bluetoothSocket != null && bluetoothSocket.isConnected();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}
