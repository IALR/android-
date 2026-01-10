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

        ListPreference themePreference = findPreference(AppSettings.KEY_THEME_MODE);
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Apply immediately and recreate to reflect changes
                    AppSettings.applyTheme(requireContext());
                    requireActivity().recreate();
                    return true;
                }
            });
        }
    }
}
