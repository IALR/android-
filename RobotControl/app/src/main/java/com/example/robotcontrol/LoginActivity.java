package com.example.robotcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robotcontrol.database.DatabaseHelper;
import com.example.robotcontrol.models.User;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RobotControlPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ProgressBar progressBar;
    private TextView forgotPasswordText;

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());
        forgotPasswordText.setOnClickListener(v -> resetPassword());

        // Check if user is already logged in
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMain();
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Check credentials against local database
        User user = dbHelper.getUserByEmail(email);
        
        progressBar.setVisibility(View.GONE);
        
        if (user != null && user.getPassword().equals(password)) {
            // Save login state
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_EMAIL, user.getEmail());
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
            
            Toast.makeText(this, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, getString(R.string.toast_login_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.error_password_min_length));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Check if user already exists
        User existingUser = dbHelper.getUserByEmail(email);
        
        if (existingUser != null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.toast_user_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new user
        String oderId = java.util.UUID.randomUUID().toString();
        User newUser = new User(oderId, email, email.split("@")[0], password);
        
        long result = dbHelper.addUser(newUser);
        
        progressBar.setVisibility(View.GONE);
        
        if (result != -1) {
            // Save login state
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_ID, newUser.getId());
            editor.putString(KEY_USER_EMAIL, newUser.getEmail());
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
            
            Toast.makeText(this, getString(R.string.toast_registration_success), Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, getString(R.string.toast_registration_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_enter_email_first));
            return;
        }

        Toast.makeText(this, getString(R.string.toast_password_reset_unavailable), Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_id", prefs.getString(KEY_USER_ID, "local_user"));
        startActivity(intent);
        finish();
    }
}
