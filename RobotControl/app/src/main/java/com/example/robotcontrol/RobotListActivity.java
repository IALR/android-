package com.example.robotcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.adapters.RobotAdapter;
import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RobotListActivity extends AppCompatActivity implements RobotAdapter.OnRobotClickListener {

    private static final String PREFS_NAME = "RobotControlPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private RecyclerView robotRecyclerView;
    private RobotAdapter robotAdapter;
    private List<Robot> robotList;
    private FloatingActionButton fabAddRobot;
    private ProgressBar progressBar;
    private View emptyView;
    private Toolbar toolbar;

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_list);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Check if user is logged in
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        currentUserId = prefs.getString(KEY_USER_ID, "local_user");
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.my_robots);
        }

        robotRecyclerView = findViewById(R.id.robotRecyclerView);
        fabAddRobot = findViewById(R.id.fab);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        // Setup RecyclerView
        robotList = new ArrayList<>();
        robotAdapter = new RobotAdapter(this, robotList, this);
        robotRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        robotRecyclerView.setAdapter(robotAdapter);

        fabAddRobot.setOnClickListener(v -> {
            Intent intent = new Intent(RobotListActivity.this, PairingActivity.class);
            intent.putExtra("user_id", currentUserId);
            startActivity(intent);
        });

        loadRobots();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRobots();
    }

    private void loadRobots() {
        progressBar.setVisibility(View.VISIBLE);
        robotList.clear();

        // Load from local database
        List<Robot> localRobots = dbHelper.getAllRobots();
        robotList.addAll(localRobots);

        updateUI();
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        if (robotList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            robotRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            robotRecyclerView.setVisibility(View.VISIBLE);
        }
        robotAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRobotClick(Robot robot) {
        if (robot == null) return;

        // WiFi robots must use the WiFi control screen (ControlActivity is Bluetooth-only)
        if ("wifi".equalsIgnoreCase(robot.getConnectionType())) {
            Intent intent = new Intent(RobotListActivity.this, RobotControlActivity.class);
            intent.putExtra("robot_id", robot.getId());
            intent.putExtra("robot_name", robot.getName());
            intent.putExtra("robot_ip", robot.getIpAddress());
            intent.putExtra("robot_port", 8888);
            intent.putExtra("robot_ssid", robot.getMacAddress());
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(RobotListActivity.this, ControlActivity.class);
        intent.putExtra("robot_id", robot.getId());
        intent.putExtra("robot_name", robot.getName());
        startActivity(intent);
    }

    @Override
    public void onRobotLongClick(Robot robot) {
        // Check if user is owner
        if (robot.getOwnerId().equals(currentUserId) || robot.getOwnerId().equals("local_user")) {
            Intent intent = new Intent(RobotListActivity.this, SettingsActivity.class);
            intent.putExtra("robot_id", robot.getId());
            intent.putExtra("robot_name", robot.getName());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Only the owner can access settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_robot_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // Clear login state
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
            editor.remove(KEY_USER_ID);
            editor.apply();
            
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_refresh) {
            loadRobots();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
