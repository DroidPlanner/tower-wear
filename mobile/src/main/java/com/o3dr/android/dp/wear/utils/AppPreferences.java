package com.o3dr.android.dp.wear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.o3dr.android.dp.wear.R;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;

/**
 * Provides structured access to the app preferences.
 */
public class AppPreferences {

    /*
    * Default preference values
    */
    public static final boolean DEFAULT_USAGE_STATISTICS = true;

    public final SharedPreferences prefs;
    private final Context context;

    public AppPreferences(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * @return true if google analytics reporting is enabled.
     */
    public boolean isUsageStatisticsEnabled() {
        return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key), DEFAULT_USAGE_STATISTICS);
    }

}
