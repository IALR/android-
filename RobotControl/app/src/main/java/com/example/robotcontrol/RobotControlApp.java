package com.example.robotcontrol;

import android.app.Application;

public class RobotControlApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings.applyTheme(this);
    }
}
