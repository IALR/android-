package com.example.robotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.robotcontrol.utils.EducationProgressStore;

/**
 * EducationHomeActivity - entry point for education features.
 * Separates Learn content and Quiz into distinct destinations.
 */
public class EducationHomeActivity extends AppCompatActivity {

    private CardView cardLearn;
    private CardView cardQuiz;
    private CardView cardRoboticsGame;
    private CardView cardCircuitBuilder;

    private TextView tvRoboticsProgress;
    private TextView tvCircuitProgress;
    private TextView tvQuizProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_home);

        cardLearn = findViewById(R.id.cardLearn);
        cardRoboticsGame = findViewById(R.id.cardRoboticsGame);
        cardCircuitBuilder = findViewById(R.id.cardCircuitBuilder);
        cardQuiz = findViewById(R.id.cardQuiz);

        tvRoboticsProgress = findViewById(R.id.tvRoboticsProgress);
        tvCircuitProgress = findViewById(R.id.tvCircuitProgress);
        tvQuizProgress = findViewById(R.id.tvQuizProgress);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.education_hub_title));
        }

        cardLearn.setOnClickListener(v -> startWithCardAnim(cardLearn, new Intent(EducationHomeActivity.this, EducationActivity.class)));
        cardQuiz.setOnClickListener(v -> startWithCardAnim(cardQuiz, new Intent(EducationHomeActivity.this, QuizActivity.class)));
        cardRoboticsGame.setOnClickListener(v -> startWithCardAnim(cardRoboticsGame, new Intent(EducationHomeActivity.this, RoboticsGameActivity.class)));
        cardCircuitBuilder.setOnClickListener(v -> startWithCardAnim(cardCircuitBuilder, new Intent(EducationHomeActivity.this, CircuitBuilderActivity.class)));

        renderProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderProgress();
    }

    private void renderProgress() {
        if (tvRoboticsProgress != null) {
            boolean done = EducationProgressStore.isRoboticsGameCompleted(this);
            tvRoboticsProgress.setText(done ? getString(R.string.education_progress_completed) : getString(R.string.education_progress_not_completed));
        }
        if (tvCircuitProgress != null) {
            boolean done = EducationProgressStore.isCircuitBuilderCompleted(this);
            tvCircuitProgress.setText(done ? getString(R.string.education_progress_completed) : getString(R.string.education_progress_not_completed));
        }
        if (tvQuizProgress != null) {
            int bestScore = EducationProgressStore.getQuizBestScore(this);
            int bestTotal = EducationProgressStore.getQuizBestTotal(this);
            if (bestScore >= 0 && bestTotal > 0) {
                tvQuizProgress.setText(getString(R.string.quiz_best_format, bestScore, bestTotal));
            } else {
                tvQuizProgress.setText(getString(R.string.education_progress_no_score));
            }
        }
    }

    private void startWithCardAnim(CardView card, Intent intent) {
        if (card == null) {
            startActivity(intent);
            return;
        }

        card.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(90)
                .withEndAction(() -> {
                    card.animate().scaleX(1f).scaleY(1f).setDuration(90).start();
                    startActivity(intent);
                })
                .start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
