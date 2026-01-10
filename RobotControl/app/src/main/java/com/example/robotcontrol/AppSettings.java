package com.example.robotcontrol;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public final class AppSettings {

    private AppSettings() {
    }

    public static final String KEY_THEME_MODE = "pref_theme_mode";

    // Values stored in prefs
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    public static void applyTheme(Context context) {
        String mode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_THEME_MODE, THEME_SYSTEM);

        if (THEME_LIGHT.equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (THEME_DARK.equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
