package com.example.catchy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

public class SettingsPrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_pref);
        getFragmentManager().beginTransaction()
                .replace(R.id.fm_pref, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        private Preference updateBio;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
            updateBio = findPreference("updatebio");
            updateBio.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            });

        }
    }
}