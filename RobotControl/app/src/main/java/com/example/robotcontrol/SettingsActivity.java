package com.example.robotcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.adapters.PermissionAdapter;
import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.Robot;
import com.example.robotcontrol.models.RobotPermission;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements PermissionAdapter.OnPermissionActionListener {

    private static final String PREFS_NAME = "RobotControlPrefs";
    private static final String KEY_USER_ID = "user_id";

    private TextView robotNameText, robotTypeText, robotMacText;
    private Button addPermissionButton, deleteRobotButton;
    private RecyclerView permissionsRecyclerView;
    private PermissionAdapter permissionAdapter;
    private List<RobotPermission> permissionList;
    private Toolbar toolbar;

    private String robotId;
    private String robotName;
    private Robot robot;
    private DatabaseHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get robot info from intent
        robotId = getIntent().getStringExtra("robot_id");
        robotName = getIntent().getStringExtra("robot_name");

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_USER_ID, "local_user");
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.robot_settings);
        }

        robotNameText = findViewById(R.id.robotNameText);
        robotTypeText = findViewById(R.id.robotTypeText);
        robotMacText = findViewById(R.id.macAddressText);
        addPermissionButton = findViewById(R.id.addPermissionButton);
        deleteRobotButton = findViewById(R.id.deleteButton);
        permissionsRecyclerView = findViewById(R.id.permissionsRecyclerView);

        // Setup RecyclerView
        permissionList = new ArrayList<>();
        permissionAdapter = new PermissionAdapter(this, permissionList, this);
        permissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionsRecyclerView.setAdapter(permissionAdapter);

        // Load robot details
        loadRobotDetails();
        loadPermissions();

        addPermissionButton.setOnClickListener(v -> showAddPermissionDialog());
        deleteRobotButton.setOnClickListener(v -> confirmDeleteRobot());
    }

    private void loadRobotDetails() {
        robot = dbHelper.getRobot(robotId);
        if (robot != null) {
            robotNameText.setText(robot.getName());
            robotTypeText.setText(robot.getType());
            robotMacText.setText(robot.getMacAddress());
        }
    }

    private void loadPermissions() {
        permissionList.clear();
        List<RobotPermission> permissions = dbHelper.getPermissionsForRobot(robotId);
        permissionList.addAll(permissions);
        permissionAdapter.notifyDataSetChanged();
    }

    private void showAddPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_permission, null);

        EditText emailInput = dialogView.findViewById(R.id.emailInput);

        builder.setView(dialogView)
                .setTitle(R.string.add_permission)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(email)) {
                        addPermission(email);
                    } else {
                        Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addPermission(String email) {
        // In local-only mode, we just store the email
        String oderId = java.util.UUID.randomUUID().toString();
        RobotPermission permission = new RobotPermission(robotId, oderId, email, true);
        
        long result = dbHelper.addPermission(permission);
        
        if (result != -1) {
            Toast.makeText(this, "Permission granted to " + email, Toast.LENGTH_SHORT).show();
            loadPermissions();
        } else {
            Toast.makeText(this, "Failed to add permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRevokePermission(RobotPermission permission) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.revoke_permission)
                .setMessage("Revoke access for " + permission.getUserEmail() + "?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deletePermission(permission.getRobotId(), permission.getUserId());
                    Toast.makeText(this, "Permission revoked", Toast.LENGTH_SHORT).show();
                    loadPermissions();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void confirmDeleteRobot() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_robot)
                .setMessage(R.string.confirm_delete_robot)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteRobot())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteRobot() {
        // Delete from local database
        dbHelper.deleteRobot(robotId);
        
        // Delete associated permissions
        dbHelper.deletePermissionsForRobot(robotId);

        Toast.makeText(this, "Robot deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
