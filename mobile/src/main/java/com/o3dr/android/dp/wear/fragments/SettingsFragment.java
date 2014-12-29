package com.o3dr.android.dp.wear.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.utils.AppPreferences;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;

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
        setupTelemetryConnectionTypePref();
        setupConnectionPreferencesScreen();
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

    private void setupConnectionPreferencesScreen() {
        final String[] prefKeys = {
                getString(R.string.pref_baud_type_key),
                getString(R.string.pref_server_port_key),
                getString(R.string.pref_server_ip_key),
                getString(R.string.pref_udp_server_port_key),
                getString(R.string.pref_bluetooth_device_address_key)
        };

        for(String prefKey: prefKeys){
            Preference pref = findPreference(prefKey);
            if(pref != null){
                pref.setSummary(appPrefs.prefs.getString(prefKey, ""));
            }
        }
    }

    private void setupTelemetryConnectionTypePref() {
        ListPreference connectionTypePref = (ListPreference) findPreference(getString(R.string.pref_connection_type_key));
        if (connectionTypePref != null) {
            int defaultConnectionType = appPrefs.getConnectionParameterType();
            updateConnectionPreferenceSummary(connectionTypePref, defaultConnectionType);
            connectionTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int connectionType = Integer.parseInt((String) newValue);
                    updateConnectionPreferenceSummary(preference, connectionType);
                    return true;
                }
            });
        }
    }

    private void updateConnectionPreferenceSummary(Preference preference, int connectionType) {
        String connectionName;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                connectionName = "USB";
                break;

            case ConnectionType.TYPE_UDP:
                connectionName = "UDP";
                break;

            case ConnectionType.TYPE_TCP:
                connectionName = "TCP";
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                connectionName = "BLUETOOTH";
                break;

            default:
                connectionName = null;
                break;
        }

        if (connectionName != null)
            preference.setSummary(connectionName);
    }
}
