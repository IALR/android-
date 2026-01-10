package com.example.robotcontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class EducationProgressStore {

    private static final String PREFS = "education_progress";

    private static final String KEY_QUIZ_BEST_SCORE = "quiz_best_score";
    private static final String KEY_QUIZ_BEST_TOTAL = "quiz_best_total";
    private static final String KEY_QUIZ_LAST_SCORE = "quiz_last_score";
    private static final String KEY_QUIZ_LAST_TOTAL = "quiz_last_total";

    private static final String KEY_GAME_ROBOTICS_DONE = "game_robotics_done";
    private static final String KEY_GAME_CIRCUIT_DONE = "game_circuit_done";

    private EducationProgressStore() {}

    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void saveQuizResult(@NonNull Context context, int score, int total) {
        if (total <= 0) return;
        if (score < 0) score = 0;
        if (score > total) score = total;

        SharedPreferences p = prefs(context);
        int bestScore = p.getInt(KEY_QUIZ_BEST_SCORE, -1);
        int bestTotal = p.getInt(KEY_QUIZ_BEST_TOTAL, total);

        boolean updateBest;
        if (bestScore < 0) {
            updateBest = true;
        } else if (bestTotal == total) {
            updateBest = score > bestScore;
        } else {
            // If the quiz length changes, keep the best for the current total only.
            updateBest = total != bestTotal;
        }

        SharedPreferences.Editor e = p.edit();
        e.putInt(KEY_QUIZ_LAST_SCORE, score);
        e.putInt(KEY_QUIZ_LAST_TOTAL, total);
        if (updateBest) {
            e.putInt(KEY_QUIZ_BEST_SCORE, score);
            e.putInt(KEY_QUIZ_BEST_TOTAL, total);
        }
        e.apply();
    }

    public static int getQuizBestScore(@NonNull Context context) {
        return prefs(context).getInt(KEY_QUIZ_BEST_SCORE, -1);
    }

    public static int getQuizBestTotal(@NonNull Context context) {
        return prefs(context).getInt(KEY_QUIZ_BEST_TOTAL, 0);
    }

    public static int getQuizLastScore(@NonNull Context context) {
        return prefs(context).getInt(KEY_QUIZ_LAST_SCORE, -1);
    }

    public static int getQuizLastTotal(@NonNull Context context) {
        return prefs(context).getInt(KEY_QUIZ_LAST_TOTAL, 0);
    }

    public static void markRoboticsGameCompleted(@NonNull Context context) {
        prefs(context).edit().putBoolean(KEY_GAME_ROBOTICS_DONE, true).apply();
    }

    public static boolean isRoboticsGameCompleted(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_GAME_ROBOTICS_DONE, false);
    }

    public static void markCircuitBuilderCompleted(@NonNull Context context) {
        prefs(context).edit().putBoolean(KEY_GAME_CIRCUIT_DONE, true).apply();
    }

    public static boolean isCircuitBuilderCompleted(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_GAME_CIRCUIT_DONE, false);
    }
}
