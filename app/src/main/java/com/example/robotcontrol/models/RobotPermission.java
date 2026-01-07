package com.example.robotcontrol.models;

public class RobotPermission {
    private String robotId;
    private String userId;
    private String userEmail;
    private boolean canControl;
    private long grantedAt;

    public RobotPermission() {
        // Empty constructor for local database
    }

    public RobotPermission(String robotId, String userId, String userEmail, boolean canControl) {
        this.robotId = robotId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.canControl = canControl;
        this.grantedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isCanControl() {
        return canControl;
    }

    public void setCanControl(boolean canControl) {
        this.canControl = canControl;
    }

    public long getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(long grantedAt) {
        this.grantedAt = grantedAt;
    }
}
