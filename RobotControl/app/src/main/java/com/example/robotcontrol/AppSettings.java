package com.example.robotcontrol;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

public final class AppSettings {

    private AppSettings() {
    }

    public static final String KEY_THEME_MODE = "pref_theme_mode";

    public static final String KEY_LANGUAGE = "pref_language";

    // Values stored in prefs
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    // Values stored in prefs (BCP-47 language tags)
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_FR = "fr";
    public static final String LANGUAGE_ES = "es";

    public static void applyTheme(Context context) {
        String mode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_THEME_MODE, THEME_SYSTEM);

        applyThemeValue(mode);
    }

    public static void applyThemeValue(String mode) {
        if (THEME_LIGHT.equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (THEME_DARK.equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void applyLanguage(Context context) {
        String tag = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LANGUAGE, LANGUAGE_EN);
        applyLanguageTag(tag);
    }

    public static void applyLanguageTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            tag = LANGUAGE_EN;
        }

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
    }
}
