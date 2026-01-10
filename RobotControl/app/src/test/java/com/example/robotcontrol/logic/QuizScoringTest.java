package com.example.robotcontrol.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuizScoringTest {

    @Test
    public void computeScore_countsCorrectAnswers() {
        int[] correct = new int[]{0, 2, 1, 3};
        int[] user = new int[]{0, 1, 1, -1};
        // Q1 correct, Q2 wrong, Q3 correct, Q4 not answered
        assertEquals(2, QuizScoring.computeScore(correct, user));
    }
}
