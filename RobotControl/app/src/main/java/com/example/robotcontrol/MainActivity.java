package com.example.robotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * Main Dashboard Activity - Home screen of the app
 * Provides navigation to all major features
 */
public class MainActivity extends AppCompatActivity {
    
    private TextView tvWelcome;
    private CardView cardRobotList, cardEducation, cardSettings, cardQuickConnect;
    private Button btnGetStarted;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);
        
        initializeViews();
        setupListeners();
        loadUserData();
    }
    
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cardRobotList = findViewById(R.id.cardRobotList);
        cardEducation = findViewById(R.id.cardEducation);
        cardSettings = findViewById(R.id.cardSettings);
        cardQuickConnect = findViewById(R.id.cardQuickConnect);
        btnGetStarted = findViewById(R.id.btnGetStarted);
    }
    
    private void setupListeners() {
        // Navigate to Robot List
        cardRobotList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RobotListActivity.class);
            startActivity(intent);
        });
        
        // Navigate to Education Hub
        cardEducation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EducationActivity.class);
            startActivity(intent);
        });
        
        // Navigate to Settings
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppSettingsActivity.class);
            startActivity(intent);
        });
        
        // Quick Connect - Navigate to Pairing
        cardQuickConnect.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PairingActivity.class);
            startActivity(intent);
        });
        
        // Get Started button
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RobotListActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadUserData() {
        // Load user information from SharedPreferences or Database
        // Display welcome message with user name
        String userName = getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Welcome back, " + userName + "!");
        } else {
            tvWelcome.setText("Welcome to RoboConnect!");
        }
    }
    
    private String getUserName() {
        // Retrieve user name from SharedPreferences
        return getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("user_name", "User");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data
        loadUserData();
    }
}