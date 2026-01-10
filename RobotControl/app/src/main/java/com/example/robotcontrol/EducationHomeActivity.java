package com.example.robotcontrol;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * EducationHomeActivity - entry point for education features.
 * Separates Learn content and Quiz into distinct destinations.
 */
public class EducationHomeActivity extends AppCompatActivity {

    private CardView cardLearn;
    private CardView cardQuiz;
    private CardView cardRoboticsGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_home);

        cardLearn = findViewById(R.id.cardLearn);
        cardRoboticsGame = findViewById(R.id.cardRoboticsGame);
        cardQuiz = findViewById(R.id.cardQuiz);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.education_hub_title));
        }

        cardLearn.setOnClickListener(v -> {
            Intent intent = new Intent(EducationHomeActivity.this, EducationActivity.class);
            startActivity(intent);
        });

        cardQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(EducationHomeActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        cardRoboticsGame.setOnClickListener(v -> {
            Intent intent = new Intent(EducationHomeActivity.this, RoboticsGameActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
