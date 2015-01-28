package com.o3dr.android.dp.wear.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.utils.AppPreferences;

/**
 * Created by fhuya on 12/27/14.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private AppPreferences appPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        appPrefs = new AppPreferences(getActivity().getApplicationContext());

        setupVersionPref();
    }

    private void setupVersionPref() {
        try {
            final Context context = getActivity().getApplicationContext();
            Preference versionPref = findPreference("pref_version");
            if (versionPref != null) {
                String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                versionPref.setSummary(version);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve version name.", e);
        }
    }
}
