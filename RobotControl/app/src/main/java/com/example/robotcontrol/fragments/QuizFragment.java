package com.example.robotcontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.robotcontrol.R;

/**
 * Fragment for Quiz functionality
 */
public class QuizFragment extends Fragment {
    
    private TextView tvQuestion;
    private RadioGroup rgOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnSubmit, btnNext;
    
    private int currentQuestion = 0;
    private int score = 0;
    private boolean isQuestionAnswered = false;

    private static final String KEY_CURRENT_QUESTION = "quiz_current_question";
    private static final String KEY_SCORE = "quiz_score";
    private static final String KEY_ANSWERED = "quiz_answered";
    private static final String KEY_CHECKED_ID = "quiz_checked_id";
    
    // Sample questions - can be loaded from database
    private String[] questions = {
        "What is Ohm's Law?",
        "What does LED stand for?",
        "Which sensor is used to measure distance?"
    };
    
    private String[][] options = {
        {"V = I × R", "P = V × I", "R = V + I", "I = V - R"},
        {"Light Emitting Diode", "Low Energy Device", "Long Electric Display", "Linear Electronic Driver"},
        {"Temperature Sensor", "Ultrasonic Sensor", "Pressure Sensor", "Humidity Sensor"}
    };
    
    private int[] correctAnswers = {0, 0, 1}; // Index of correct answer
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        
        initializeViews(view);
        setupListeners();

        if (!isQuizDataValid()) {
            showInvalidQuizData();
            return view;
        }

        if (savedInstanceState != null) {
            currentQuestion = savedInstanceState.getInt(KEY_CURRENT_QUESTION, 0);
            score = savedInstanceState.getInt(KEY_SCORE, 0);
            isQuestionAnswered = savedInstanceState.getBoolean(KEY_ANSWERED, false);
            int checkedId = savedInstanceState.getInt(KEY_CHECKED_ID, -1);

            renderQuestion(false);
            if (checkedId != -1) {
                rgOptions.check(checkedId);
            }
            if (isQuestionAnswered) {
                applyAnsweredState();
            } else {
                applyUnansweredState();
            }
        } else {
            renderQuestion(true);
        }
        
        return view;
    }
    
    private void initializeViews(View view) {
        tvQuestion = view.findViewById(R.id.tvQuestion);
        rgOptions = view.findViewById(R.id.rgOptions);
        rbOption1 = view.findViewById(R.id.rbOption1);
        rbOption2 = view.findViewById(R.id.rbOption2);
        rbOption3 = view.findViewById(R.id.rbOption3);
        rbOption4 = view.findViewById(R.id.rbOption4);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnNext = view.findViewById(R.id.btnNext);
    }
    
    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());
    }
    
    private void renderQuestion(boolean clearSelection) {
        if (currentQuestion < 0) currentQuestion = 0;

        if (currentQuestion < questions.length) {
            isQuestionAnswered = false;
            tvQuestion.setText(questions[currentQuestion]);
            rbOption1.setText(options[currentQuestion][0]);
            rbOption2.setText(options[currentQuestion][1]);
            rbOption3.setText(options[currentQuestion][2]);
            rbOption4.setText(options[currentQuestion][3]);

            if (clearSelection) {
                rgOptions.clearCheck();
            }

            applyUnansweredState();
        } else {
            showResults();
        }
    }
    
    private void checkAnswer() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int selectedIndex = -1;
        if (selectedId == rbOption1.getId()) selectedIndex = 0;
        else if (selectedId == rbOption2.getId()) selectedIndex = 1;
        else if (selectedId == rbOption3.getId()) selectedIndex = 2;
        else if (selectedId == rbOption4.getId()) selectedIndex = 3;
        
        if (selectedIndex == correctAnswers[currentQuestion]) {
            score++;
            Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Wrong! Correct answer: " + 
                options[currentQuestion][correctAnswers[currentQuestion]], Toast.LENGTH_LONG).show();
        }

        isQuestionAnswered = true;
        applyAnsweredState();
    }
    
    private void nextQuestion() {
        currentQuestion++;
        renderQuestion(true);
    }
    
    private void showResults() {
        tvQuestion.setText("Quiz Complete!\n\nYour Score: " + score + "/" + questions.length);
        rgOptions.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        
        // Save score to database
    }

    private void applyAnsweredState() {
        btnSubmit.setEnabled(false);
        btnNext.setEnabled(true);
        setOptionsEnabled(false);
    }

    private void applyUnansweredState() {
        btnSubmit.setEnabled(true);
        btnNext.setEnabled(false);
        setOptionsEnabled(true);
    }

    private void setOptionsEnabled(boolean enabled) {
        rbOption1.setEnabled(enabled);
        rbOption2.setEnabled(enabled);
        rbOption3.setEnabled(enabled);
        rbOption4.setEnabled(enabled);
    }

    private boolean isQuizDataValid() {
        if (questions == null || options == null || correctAnswers == null) return false;
        if (questions.length == 0) return false;
        if (options.length != questions.length) return false;
        if (correctAnswers.length != questions.length) return false;

        for (int i = 0; i < questions.length; i++) {
            if (options[i] == null || options[i].length != 4) return false;
            int correct = correctAnswers[i];
            if (correct < 0 || correct > 3) return false;
        }
        return true;
    }

    private void showInvalidQuizData() {
        tvQuestion.setText("Quiz data is invalid. Please update the questions/options.");
        rgOptions.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_QUESTION, currentQuestion);
        outState.putInt(KEY_SCORE, score);
        outState.putBoolean(KEY_ANSWERED, isQuestionAnswered);
        outState.putInt(KEY_CHECKED_ID, rgOptions != null ? rgOptions.getCheckedRadioButtonId() : -1);
    }
}
