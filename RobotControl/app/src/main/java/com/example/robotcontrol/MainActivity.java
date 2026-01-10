package com.example.robotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.adapters.RobotPreviewAdapter;
import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Dashboard Activity - Home screen of the app
 * Provides navigation to all major features
 */
public class MainActivity extends AppCompatActivity {
    
    private TextView tvWelcome;
    private CardView cardRobotList, cardEducation, cardSettings, cardQuickConnect;
    private Button btnGetStarted;

    private TextView btnViewAllRobots;
    private TextView tvRobotPreviewEmpty;
    private RecyclerView rvRobotPreview;
    private RobotPreviewAdapter robotPreviewAdapter;
    private final List<Robot> robotPreviewList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);

        dbHelper = new DatabaseHelper(this);
        
        initializeViews();
        setupListeners();
        loadUserData();
        loadRobotPreview();
    }
    
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cardRobotList = findViewById(R.id.cardRobotList);
        cardEducation = findViewById(R.id.cardEducation);
        cardSettings = findViewById(R.id.cardSettings);
        cardQuickConnect = findViewById(R.id.cardQuickConnect);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        btnViewAllRobots = findViewById(R.id.btnViewAllRobots);
        tvRobotPreviewEmpty = findViewById(R.id.tvRobotPreviewEmpty);
        rvRobotPreview = findViewById(R.id.rvRobotPreview);

        robotPreviewAdapter = new RobotPreviewAdapter(this, robotPreviewList, robot -> {
            // Keep behavior simple: tapping a preview item opens the full list
            openRobotList();
        });
        rvRobotPreview.setLayoutManager(new LinearLayoutManager(this));
        rvRobotPreview.setAdapter(robotPreviewAdapter);
    }
    
    private void setupListeners() {
        // Navigate to Robot List
        cardRobotList.setOnClickListener(v -> {
            openRobotList();
        });

        btnViewAllRobots.setOnClickListener(v -> openRobotList());
        
        // Navigate to Education Hub
        cardEducation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EducationHomeActivity.class);
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
            openRobotList();
        });
    }

    private void openRobotList() {
        Intent intent = new Intent(MainActivity.this, RobotListActivity.class);
        startActivity(intent);
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

    private void loadRobotPreview() {
        robotPreviewList.clear();
        try {
            List<Robot> allRobots = dbHelper.getAllRobots();
            if (allRobots != null) {
                int count = Math.min(allRobots.size(), 3);
                for (int i = 0; i < count; i++) {
                    robotPreviewList.add(allRobots.get(i));
                }
            }
        } catch (Exception e) {
            // keep UI stable
        }

        boolean hasRobots = !robotPreviewList.isEmpty();
        tvRobotPreviewEmpty.setVisibility(hasRobots ? View.GONE : View.VISIBLE);
        rvRobotPreview.setVisibility(hasRobots ? View.VISIBLE : View.GONE);
        robotPreviewAdapter.notifyDataSetChanged();
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
        loadRobotPreview();
    }
}