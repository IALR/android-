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
        loadQuestion();
        
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
    
    private void loadQuestion() {
        if (currentQuestion < questions.length) {
            tvQuestion.setText(questions[currentQuestion]);
            rbOption1.setText(options[currentQuestion][0]);
            rbOption2.setText(options[currentQuestion][1]);
            rbOption3.setText(options[currentQuestion][2]);
            rbOption4.setText(options[currentQuestion][3]);
            rgOptions.clearCheck();
            btnNext.setEnabled(false);
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
        
        btnSubmit.setEnabled(false);
        btnNext.setEnabled(true);
    }
    
    private void nextQuestion() {
        currentQuestion++;
        btnSubmit.setEnabled(true);
        loadQuestion();
    }
    
    private void showResults() {
        tvQuestion.setText("Quiz Complete!\n\nYour Score: " + score + "/" + questions.length);
        rgOptions.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        
        // Save score to database
    }
}
