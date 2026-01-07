package com.example.robotcontrol.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String email;
    private String name;
    private String password;
    private List<String> ownedRobots;
    private List<String> sharedRobots;

    public User() {
        // Empty constructor for local database
        this.ownedRobots = new ArrayList<>();
        this.sharedRobots = new ArrayList<>();
    }

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.ownedRobots = new ArrayList<>();
        this.sharedRobots = new ArrayList<>();
    }

    public User(String id, String email, String name, String password) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.ownedRobots = new ArrayList<>();
        this.sharedRobots = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getOwnedRobots() {
        return ownedRobots;
    }

    public void setOwnedRobots(List<String> ownedRobots) {
        this.ownedRobots = ownedRobots;
    }

    public List<String> getSharedRobots() {
        return sharedRobots;
    }

    public void setSharedRobots(List<String> sharedRobots) {
        this.sharedRobots = sharedRobots;
    }

    public void addOwnedRobot(String robotId) {
        if (!ownedRobots.contains(robotId)) {
            ownedRobots.add(robotId);
        }
    }

    public void addSharedRobot(String robotId) {
        if (!sharedRobots.contains(robotId)) {
            sharedRobots.add(robotId);
        }
    }

    public void removeOwnedRobot(String robotId) {
        ownedRobots.remove(robotId);
    }

    public void removeSharedRobot(String robotId) {
        sharedRobots.remove(robotId);
    }
}
