package com.o3dr.android.dp.wear.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.o3dr.android.dp.wear.R;

/**
 * Created by fhuya on 12/27/14.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
