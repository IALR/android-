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
    private TextView connectionTypeText, ipAddressText;
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

    private boolean canManageRobot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get robot info from intent
        robotId = getIntent().getStringExtra("robot_id");
        robotName = getIntent().getStringExtra("robot_name");

        if (robotId == null || robotId.trim().isEmpty()) {
            Toast.makeText(this, "Missing robot id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        connectionTypeText = findViewById(R.id.connectionTypeText);
        ipAddressText = findViewById(R.id.ipAddressText);
        addPermissionButton = findViewById(R.id.addPermissionButton);
        deleteRobotButton = findViewById(R.id.deleteButton);
        permissionsRecyclerView = findViewById(R.id.permissionsRecyclerView);

        // Setup RecyclerView
        permissionList = new ArrayList<>();
        permissionAdapter = new PermissionAdapter(this, permissionList, false, this);
        permissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionsRecyclerView.setAdapter(permissionAdapter);

        // Load robot details
        loadRobotDetails();
        loadPermissions();

        addPermissionButton.setOnClickListener(v -> {
            if (!canManageRobot) {
                Toast.makeText(this, "Only the owner can grant access", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddPermissionDialog();
        });
        deleteRobotButton.setOnClickListener(v -> {
            if (!canManageRobot) {
                Toast.makeText(this, "Only the owner can delete this robot", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmDeleteRobot();
        });

        robotNameText.setOnClickListener(v -> {
            if (!canManageRobot) {
                Toast.makeText(this, "Only the owner can edit", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditFieldDialog("Robot Name", robotNameText.getText().toString(), value -> {
                if (robot != null) {
                    robot.setName(value);
                    dbHelper.updateRobot(robot);
                    loadRobotDetails();
                }
            });
        });

        robotTypeText.setOnClickListener(v -> {
            if (!canManageRobot) {
                Toast.makeText(this, "Only the owner can edit", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditFieldDialog("Robot Type", robotTypeText.getText().toString(), value -> {
                if (robot != null) {
                    robot.setType(value);
                    dbHelper.updateRobot(robot);
                    loadRobotDetails();
                }
            });
        });
    }

    private void loadRobotDetails() {
        robot = dbHelper.getRobot(robotId);
        if (robot == null) {
            Toast.makeText(this, "Robot not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        canManageRobot = robot.getOwnerId() != null
                && (robot.getOwnerId().equals(currentUserId) || robot.getOwnerId().equals("local_user"));

        robotNameText.setText(robot.getName());
        robotTypeText.setText(robot.getType());
        robotMacText.setText(robot.getMacAddress());

        if (connectionTypeText != null) {
            String ct = robot.getConnectionType();
            connectionTypeText.setText(ct == null || ct.trim().isEmpty() ? "unknown" : ct);
        }
        if (ipAddressText != null) {
            String ip = robot.getIpAddress();
            ipAddressText.setText(ip == null || ip.trim().isEmpty() ? "-" : ip);
        }

        // Update adapter button visibility based on ownership
        permissionAdapter.setCanRevoke(canManageRobot);
        permissionAdapter.notifyDataSetChanged();

        // Hide dangerous actions for non-owners
        if (!canManageRobot) {
            addPermissionButton.setEnabled(false);
            deleteRobotButton.setEnabled(false);
            deleteRobotButton.setAlpha(0.5f);
            addPermissionButton.setAlpha(0.5f);
        }
    }

    private void loadPermissions() {
        permissionList.clear();
        List<RobotPermission> permissions = dbHelper.getPermissionsForRobot(robotId);
        permissionList.addAll(permissions);
        permissionAdapter.notifyDataSetChanged();
    }

    private interface OnValueSaved {
        void onSaved(String value);
    }

    private void showEditFieldDialog(String title, String initialValue, OnValueSaved onSaved) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_permission, null);

        EditText input = dialogView.findViewById(R.id.emailInput);
        input.setHint(title);
        input.setText(initialValue);

        builder.setView(dialogView)
                .setTitle(title)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, "Value is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    onSaved.onSaved(value);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        if (!canManageRobot) {
            Toast.makeText(this, "Only the owner can revoke access", Toast.LENGTH_SHORT).show();
            return;
        }
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
