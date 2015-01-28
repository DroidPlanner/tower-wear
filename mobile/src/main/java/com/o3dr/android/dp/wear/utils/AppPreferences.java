package com.o3dr.android.dp.wear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.unit.systems.UnitSystem;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;

/**
 * Provides structured access to the app preferences.
 */
public class AppPreferences {

    /*
    * Default preference values
    */
    public static final boolean DEFAULT_USAGE_STATISTICS = true;
    private static final boolean DEFAULT_IS_GPS_HDOP_ENABLED = false;
    private static final boolean DEFAULT_IS_NOTIFICATION_PERMANENT = true;
    private static final boolean DEFAULT_IS_SCREEN_BRIGHT = true;
    private static final int DEFAULT_UNIT_SYSTEM = UnitSystem.AUTO;

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

    public boolean isGpsHdopEnabled(){
        return prefs.getBoolean(context.getString(R.string.pref_ui_gps_hdop_key), DEFAULT_IS_GPS_HDOP_ENABLED);
    }

    public boolean isNotificationPermanent(){
        return prefs.getBoolean(context.getString(R.string.pref_permanent_notification_key),
                DEFAULT_IS_NOTIFICATION_PERMANENT);
    }

    public boolean keepScreenBright(){
        return prefs.getBoolean(context.getString(R.string.pref_keep_screen_bright_key), DEFAULT_IS_SCREEN_BRIGHT);
    }

    public int getUnitSystemType() {
        String unitSystem = prefs.getString(context.getString(R.string.pref_unit_system_key), null);
        if(unitSystem == null)
            return DEFAULT_UNIT_SYSTEM;

        return Integer.parseInt(unitSystem);
    }

}
