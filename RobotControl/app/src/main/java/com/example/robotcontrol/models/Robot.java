package com.example.robotcontrol.models;

public class Robot {
    private String id;
    private String name;
    private String macAddress;
    private String ipAddress;
    private String type;
    private String ownerId;
    private boolean isConnected;
    private String connectionType; // "bluetooth" or "wifi"
    private long lastConnected;

    public Robot() {
        // Empty constructor for Firebase
    }

    public Robot(String id, String name, String macAddress, String ipAddress, String type, String ownerId) {
        this.id = id;
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.type = type;
        this.ownerId = ownerId;
        this.isConnected = false;
        this.connectionType = "bluetooth";
        this.lastConnected = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public long getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(long lastConnected) {
        this.lastConnected = lastConnected;
    }
}
