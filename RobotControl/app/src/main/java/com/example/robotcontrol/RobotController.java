package com.example.robotcontrol;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RobotController {
    private static final String TAG = "RobotController";
    private String robotIP = "192.168.4.1";
    private int robotPort = 8888;

    public interface CommandCallback {
        void onSending(String endpoint, String url);
        void onResult(String endpoint, int httpCode, String body);
        void onError(String endpoint, Exception error);
    }

    private volatile CommandCallback commandCallback;

    public void setCommandCallback(CommandCallback callback) {
        this.commandCallback = callback;
    }

    public RobotController() {
        this.robotIP = "192.168.4.1";
        this.robotPort = 8888;
    }

    public RobotController(String ipAddress, int port) {
        this.robotIP = ipAddress;
        this.robotPort = port;
    }

    private void sendRequest(final String endpoint) {
        new Thread(() -> {
            try {
                String url = "http://" + robotIP + ":" + robotPort + endpoint;
                Log.d(TAG, "Sending: " + url);

                CommandCallback cb = commandCallback;
                if (cb != null) {
                    try {
                        cb.onSending(endpoint, url);
                    } catch (Exception ignored) {
                    }
                }

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response: " + responseCode);

                InputStream stream = null;
                try {
                    stream = (responseCode >= 200 && responseCode < 300)
                            ? connection.getInputStream()
                            : connection.getErrorStream();
                } catch (Exception ignored) {
                }

                String body = "";
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.d(TAG, "Body: " + sb);
                        body = sb.toString();
                    } catch (Exception ignored) {
                    }
                }

                cb = commandCallback;
                if (cb != null) {
                    try {
                        cb.onResult(endpoint, responseCode, body);
                    } catch (Exception ignored) {
                    }
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error", e);

                CommandCallback cb = commandCallback;
                if (cb != null) {
                    try {
                        cb.onError(endpoint, e);
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    // Motor Control Commands
    public void moveForward() {
        sendRequest("/forward");
    }

    public void moveBackward() {
        sendRequest("/backward");
    }

    public void turnLeft() {
        sendRequest("/left");
    }

    public void turnRight() {
        sendRequest("/right");
    }

    public void stopMotors() {
        sendRequest("/stop");
    }

    // Servo Control Commands
    public void setServo(int servoNumber, int angle) {
        if (angle < 0 || angle > 180) {
            Log.w(TAG, "Servo angle out of range: " + angle);
            return;
        }
        String endpoint = String.format("/set?servo=%d&angle=%d", servoNumber, angle);
        sendRequest(endpoint);
    }

    // Convenience methods for named servos
    public void setBaseServo(int angle) {
        setServo(1, angle);
    }

    public void setShoulderServo(int angle) {
        setServo(2, angle);
    }

    public void setGripperServo(int angle) {
        setServo(3, angle);
    }
}
