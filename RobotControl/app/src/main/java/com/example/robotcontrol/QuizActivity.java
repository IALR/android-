package com.example.robotcontrol;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robotcontrol.fragments.QuizFragment;

/**
 * QuizActivity - standalone host for the quiz experience.
 */
public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.education_tab_quiz));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.quizContainer, new QuizFragment())
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
