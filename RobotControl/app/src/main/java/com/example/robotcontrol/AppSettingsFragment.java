package com.example.robotcontrol;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class AppSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_app, rootKey);

        ListPreference languagePreference = findPreference(AppSettings.KEY_LANGUAGE);
        if (languagePreference != null) {
            languagePreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            languagePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue instanceof String) {
                        AppSettings.applyLanguageTag((String) newValue);
                        requireActivity().recreate();
                    }
                    return true;
                }
            });
        }

        ListPreference themePreference = findPreference(AppSettings.KEY_THEME_MODE);
        if (themePreference != null) {
            themePreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Apply immediately and recreate to reflect changes
                    if (newValue instanceof String) {
                        AppSettings.applyThemeValue((String) newValue);
                        requireActivity().recreate();
                    }
                    return true;
                }
            });
        }
        
        ListPreference colorThemePreference = findPreference(AppSettings.KEY_COLOR_THEME);
        if (colorThemePreference != null) {
            colorThemePreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            colorThemePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Apply color theme immediately and recreate to reflect changes
                    if (newValue instanceof String) {
                        requireActivity().recreate();
                    }
                    return true;
                }
            });
        }
    }
}
