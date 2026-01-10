package com.example.robotcontrol;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private static final String PREFS_NAME = "RobotControlPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AnimatorSet splashAnimator;
    private final Runnable navigateRunnable = () -> {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        Intent intent;
        if (isLoggedIn) {
            // User is already logged in, go to main dashboard
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // User not logged in, go to login screen
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startSplashAnimation();

        // Delay and then navigate to appropriate screen
        handler.postDelayed(navigateRunnable, SPLASH_DURATION);
    }

    private void startSplashAnimation() {
        ImageView logo = findViewById(R.id.splashLogo);
        TextView title = findViewById(R.id.splashTitle);
        TextView tagline = findViewById(R.id.splashTagline);
        ProgressBar progress = findViewById(R.id.splashProgress);

        // Initial state
        logo.setScaleX(0.90f);
        logo.setScaleY(0.90f);
        logo.setAlpha(0f);

        title.setAlpha(0f);
        tagline.setAlpha(0f);
        progress.setAlpha(0f);

        // Logo fades in, then gently pulses + wiggles
        ObjectAnimator logoFade = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f);
        logoFade.setDuration(350);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.90f, 1.05f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.90f, 1.05f);
        logoScaleX.setDuration(650);
        logoScaleY.setDuration(650);
        logoScaleX.setRepeatMode(ValueAnimator.REVERSE);
        logoScaleY.setRepeatMode(ValueAnimator.REVERSE);
        logoScaleX.setRepeatCount(ValueAnimator.INFINITE);
        logoScaleY.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator logoRotate = ObjectAnimator.ofFloat(logo, View.ROTATION, -4f, 4f);
        logoRotate.setDuration(900);
        logoRotate.setRepeatMode(ValueAnimator.REVERSE);
        logoRotate.setRepeatCount(ValueAnimator.INFINITE);

        // Text fades in shortly after
        ObjectAnimator titleFade = ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f);
        titleFade.setStartDelay(180);
        titleFade.setDuration(450);

        ObjectAnimator taglineFade = ObjectAnimator.ofFloat(tagline, View.ALPHA, 0f, 0.8f);
        taglineFade.setStartDelay(320);
        taglineFade.setDuration(450);

        ObjectAnimator progressFade = ObjectAnimator.ofFloat(progress, View.ALPHA, 0f, 1f);
        progressFade.setStartDelay(450);
        progressFade.setDuration(350);

        splashAnimator = new AnimatorSet();
        splashAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        splashAnimator.playTogether(
                logoFade,
                logoScaleX,
                logoScaleY,
                logoRotate,
                titleFade,
                taglineFade,
                progressFade
        );
        splashAnimator.start();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        if (splashAnimator != null) {
            splashAnimator.cancel();
            splashAnimator = null;
        }
        super.onDestroy();
    }
}
