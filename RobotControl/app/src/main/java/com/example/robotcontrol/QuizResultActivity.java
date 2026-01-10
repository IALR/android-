package com.example.robotcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robotcontrol.utils.EducationProgressStore;

public class QuizResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_USER_ANSWERS = "extra_user_answers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.education_tab_quiz));
        }

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        int[] userAnswers = getIntent().getIntArrayExtra(EXTRA_USER_ANSWERS);

        // Persist best/last for Education Home
        EducationProgressStore.saveQuizResult(this, score, total);

        TextView tvTitle = findViewById(R.id.tvQuizResultTitle);
        TextView tvBest = findViewById(R.id.tvQuizResultBest);
        Button btnRetry = findViewById(R.id.btnRetryQuiz);
        Button btnReview = findViewById(R.id.btnReviewAnswers);

        tvTitle.setText(getString(R.string.quiz_complete_format, score, total));

        int bestScore = EducationProgressStore.getQuizBestScore(this);
        int bestTotal = EducationProgressStore.getQuizBestTotal(this);
        if (bestScore >= 0 && bestTotal > 0) {
            tvBest.setText(getString(R.string.quiz_best_format, bestScore, bestTotal));
        } else {
            tvBest.setText("");
        }

        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, QuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, QuizReviewActivity.class);
            intent.putExtra(EXTRA_SCORE, score);
            intent.putExtra(EXTRA_TOTAL, total);
            intent.putExtra(EXTRA_USER_ANSWERS, userAnswers);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
