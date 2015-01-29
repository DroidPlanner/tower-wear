package com.o3dr.android.dp.wear.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.AppPreferences;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;

/**
 * Created by fhuya on 12/27/14.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private AppPreferences appPrefs;
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Context context = getActivity().getApplicationContext();
        appPrefs = new AppPreferences(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        setupVersionPref();
        setupUnitSystemPreferences();

        //Checkbox preferences
        setupCheckBoxPreference(R.string.pref_ui_gps_hdop_key);
        setupCheckBoxPreference(R.string.pref_keep_screen_bright_key);
        setupCheckBoxPreference(R.string.pref_permanent_notification_key);
    }

    private void setupCheckBoxPreference(final int prefKeyId){
        Preference pref = findPreference(getString(prefKeyId));
        if (pref != null) {
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    broadcastManager.sendBroadcast(new Intent(WearUtils.ACTION_PREFERENCES_UPDATED)
                            .putExtra(WearUtils.EXTRA_PREFERENCE_KEY_ID, prefKeyId));
                    return true;
                }
            });
        }
    }

    private void setupUnitSystemPreferences() {
        ListPreference unitSystemPref = (ListPreference) findPreference(getString(R.string.pref_unit_system_key));
        if (unitSystemPref != null) {
            int defaultUnitSystem = appPrefs.getUnitSystemType();
            updateUnitSystemSummary(unitSystemPref, defaultUnitSystem);
            unitSystemPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int unitSystem = Integer.parseInt((String) newValue);
                    updateUnitSystemSummary(preference, unitSystem);
                    broadcastManager.sendBroadcast(new Intent(WearUtils.ACTION_PREFERENCES_UPDATED)
                            .putExtra(WearUtils.EXTRA_PREFERENCE_KEY_ID, R.string.pref_unit_system_key));
                    return true;
                }
            });
        }
    }

    private void updateUnitSystemSummary(Preference preference, int unitSystemType) {
        final int summaryResId;
        switch (unitSystemType) {
            case 0:
            default:
                summaryResId = R.string.unit_system_entry_auto;
                break;

            case 1:
                summaryResId = R.string.unit_system_entry_metric;
                break;

            case 2:
                summaryResId = R.string.unit_system_entry_imperial;
                break;
        }

        preference.setSummary(summaryResId);
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
