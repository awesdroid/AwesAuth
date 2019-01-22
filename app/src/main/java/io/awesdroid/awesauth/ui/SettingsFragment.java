package io.awesdroid.awesauth.ui;

import android.os.Bundle;

import java.util.Objects;

import androidx.lifecycle.ViewModelProviders;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.viewmodel.SettingsViewModel;


public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        String key = Objects.requireNonNull(getActivity())
                .getResources()
                .getString(R.string.pref_key_google_auth_type);
        setPreferencesFromResource(R.xml.settings, rootKey);
        ListPreference listPreference = findPreference(key);
        listPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        SettingsViewModel viewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);

        viewModel.getAuthType().observe(this, v -> {
            CheckBoxPreference usePendingIntentPref =
                    findPreference(getString(R.string.pref_key_use_pending_intent));
            SwitchPreference useIdTokenPref =
                    findPreference(getString(R.string.pref_key_use_id_token));
            if ("-1".equals(v)) {
                usePendingIntentPref.setEnabled(false);
                useIdTokenPref.setEnabled(false);
            } else {
                viewModel.setAuthTypeName(listPreference.getEntry().toString());
                if ("0".equals(v)) {
                    usePendingIntentPref.setEnabled(true);
                    useIdTokenPref.setEnabled(false);
                } else {
                    usePendingIntentPref.setEnabled(false);
                    useIdTokenPref.setEnabled(true);
                }
            }

        });
    }
}
