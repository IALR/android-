package com.example.robotcontrol;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QuizReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_review);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.quiz_review_title));
        }

        int[] userAnswers = getIntent().getIntArrayExtra(QuizResultActivity.EXTRA_USER_ANSWERS);
        if (userAnswers == null) {
            userAnswers = new int[0];
        }

        String[] questions = getResources().getStringArray(R.array.quiz_questions);
        int[] correct = getResources().getIntArray(R.array.quiz_correct_answers);

        String[][] options = new String[questions.length][4];
        TypedArray optionArrays = getResources().obtainTypedArray(R.array.quiz_options_arrays);
        try {
            for (int i = 0; i < questions.length; i++) {
                int resId = optionArrays.getResourceId(i, 0);
                if (resId != 0) {
                    options[i] = getResources().getStringArray(resId);
                } else {
                    options[i] = new String[]{"", "", "", ""};
                }
            }
        } finally {
            optionArrays.recycle();
        }

        LinearLayout container = findViewById(R.id.reviewContainer);
        container.removeAllViews();

        for (int i = 0; i < questions.length; i++) {
            int userIndex = (i < userAnswers.length) ? userAnswers[i] : -1;
            int correctIndex = (i < correct.length) ? correct[i] : -1;

            String userText = (userIndex >= 0 && userIndex < 4) ? options[i][userIndex] : getString(R.string.quiz_not_answered);
            String correctText = (correctIndex >= 0 && correctIndex < 4) ? options[i][correctIndex] : "";

            LinearLayout block = new LinearLayout(this);
            block.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = dp(14);
            block.setLayoutParams(lp);

            TextView tvQ = new TextView(this);
            tvQ.setText((i + 1) + ". " + questions[i]);
            tvQ.setTextColor(getColor(R.color.text_primary));
            tvQ.setTextSize(15f);
            tvQ.setTypeface(tvQ.getTypeface(), android.graphics.Typeface.BOLD);

            TextView tvYour = new TextView(this);
            tvYour.setText(getString(R.string.quiz_your_answer_format, userText));
            tvYour.setTextColor(getColor(R.color.text_secondary));
            tvYour.setTextSize(13f);

            TextView tvCorrect = new TextView(this);
            tvCorrect.setText(getString(R.string.quiz_correct_answer_format, correctText));
            tvCorrect.setTextColor(getColor(R.color.text_secondary));
            tvCorrect.setTextSize(13f);

            block.addView(tvQ);
            block.addView(tvYour);
            block.addView(tvCorrect);

            container.addView(block);
        }
    }

    private int dp(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
