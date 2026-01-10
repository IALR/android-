package com.example.robotcontrol.logic;

import androidx.annotation.NonNull;

public final class QuizScoring {

    private QuizScoring() {
    }

    /**
     * Computes the number of correct answers.
     *
     * @param correctAnswers index (0-3) per question
     * @param userAnswers    index (0-3) per question, or -1 for not answered
     */
    public static int computeScore(@NonNull int[] correctAnswers, @NonNull int[] userAnswers) {
        int count = Math.min(correctAnswers.length, userAnswers.length);
        int score = 0;
        for (int i = 0; i < count; i++) {
            if (userAnswers[i] == correctAnswers[i]) {
                score++;
            }
        }
        return score;
    }
}
