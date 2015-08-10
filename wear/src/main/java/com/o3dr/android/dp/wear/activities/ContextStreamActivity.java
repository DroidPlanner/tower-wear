package com.o3dr.android.dp.wear.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.SpannableUtils;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.lib.utils.unit.UnitManager;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.util.MathUtils;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import org.beyene.sius.unit.length.LengthUnit;

import java.util.Locale;

/**
 * Created by fhuya on 12/28/14.
 */
public class ContextStreamActivity extends BaseActivity {

    private final static String TAG = ContextStreamActivity.class.getSimpleName();

    private static final IntentFilter intentFilter = new IntentFilter(WearUtils.ACTION_PREFERENCES_UPDATED);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case WearUtils.ACTION_PREFERENCES_UPDATED:
                    final int prefKeyId = intent.getIntExtra(WearUtils.EXTRA_PREFERENCE_KEY_ID, -1);
                    switch(prefKeyId){
                        case R.string.pref_keep_screen_bright_key:
                            if(activityLayout != null)
                                activityLayout.setKeepScreenOn(appPrefs.keepScreenBright());
                            break;

                        case R.string.pref_ui_gps_hdop_key:
                            updateGpsStatus();
                            break;

                        case R.string.pref_unit_system_key:
                            updateHomeStatus();
                            break;
                    }
                    break;
            }
        }
    };

    private View activityLayout;
    private TextView signalStatus;
    private TextView batteryStatus;
    private TextView gpsStatus;
    private TextView homeStatus;
    private TextView connectionStatus;

    private Gps droneGps;
    private Home droneHome;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating context stream activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_stream);

        activityLayout = findViewById(R.id.context_stream_layout);
        signalStatus = (TextView) findViewById(R.id.bar_signal);
        batteryStatus = (TextView) findViewById(R.id.bar_battery);
        gpsStatus = (TextView) findViewById(R.id.bar_gps);
        homeStatus = (TextView) findViewById(R.id.bar_home);
        connectionStatus = (TextView) findViewById(R.id.connection_status);

        onEventReceived(getIntent());
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadVehicleData(AttributeType.STATE);
        reloadVehicleData(AttributeType.GPS);
        reloadVehicleData(AttributeType.BATTERY);
        reloadVehicleData(AttributeType.SIGNAL);
        reloadVehicleData(AttributeType.HOME);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onEventReceived(intent);
    }

    private void onEventReceived(Intent intent) {
        if (intent == null)
            return;

        final String action = intent.getAction();
        if (action == null)
            return;

        final byte[] eventData = intent.getByteArrayExtra(WearReceiverService.EXTRA_EVENT_DATA);
        onVehicleDataUpdated(action, eventData);
    }

    @Override
    protected void onVehicleDataUpdated(String dataType, byte[] eventData) {
        switch (dataType) {
            case AttributeType.STATE:
                State vehicleState = eventData == null ? null : ParcelableUtils.unmarshall(eventData, State.CREATOR);
                final boolean isConnected = vehicleState != null && vehicleState.isConnected();
                activityLayout.setKeepScreenOn(isConnected && appPrefs.keepScreenBright());

                final CharSequence connectionLabel;
                if (isConnected) {
                    VehicleMode flightMode = vehicleState.getVehicleMode();
                    final int color = Color.rgb(34, 139, 34);
                    if (flightMode == null)
                        connectionLabel = SpannableUtils.color(color, "connected");
                    else {
                        final int droneType = flightMode.getDroneType();
                        final String typeLabel;
                        switch(droneType){
                            case Type.TYPE_COPTER:
                                typeLabel = "Copter:  ";
                                break;

                            case Type.TYPE_PLANE:
                                typeLabel = "Plane:  ";
                                break;

                            case Type.TYPE_ROVER:
                                typeLabel = "Rover:  ";
                                break;

                            default:
                                typeLabel = "";
                                break;
                        }

                        connectionLabel = SpannableUtils.normal(typeLabel, SpannableUtils.color(color,
                                flightMode.getLabel()));
                    }
                } else {
                    connectionLabel = SpannableUtils.color(Color.RED, "disconnected");
                }
                connectionStatus.setText(connectionLabel);
                break;

            case AttributeType.GPS:
                droneGps = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Gps.CREATOR);
                updateGpsStatus();
                break;

            case AttributeType.BATTERY:
                Battery battery = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Battery.CREATOR);
                if (battery == null)
                    batteryStatus.setText(R.string.empty_content);
                else {
                    batteryStatus.setText(String.format(Locale.ENGLISH, "%2.1fv", battery.getBatteryVoltage()));
                }
                break;

            case AttributeType.SIGNAL:
                Signal signal = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Signal.CREATOR);
                if (signal == null || !signal.isValid()) {
                    signalStatus.setText(R.string.empty_content);
                } else {
                    final int signalStrength = MathUtils.getSignalStrength(signal.getFadeMargin(),
                            signal.getRemFadeMargin());
                    signalStatus.setText(signalStrength + "%");
                }
                break;

            case AttributeType.HOME:
                droneHome = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Home.CREATOR);
                updateHomeStatus();
                break;
        }
    }

    private void updateGpsStatus(){
        if (droneGps == null) {
            gpsStatus.setText(R.string.empty_content);
            gpsStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_off_grey600_24dp, 0, 0, 0);
        } else {
            updateHomeStatus();

            final boolean isHdopEnabled = appPrefs.isGpsHdopEnabled();
            switch (droneGps.getFixStatus()) {
                case Gps.NO_FIX:
                    gpsStatus.setText(R.string.empty_content);
                    gpsStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_off_grey600_24dp, 0, 0, 0);
                    break;

                case Gps.LOCK_2D:
                    gpsStatus.setText(isHdopEnabled
                            ? String.valueOf(droneGps.getGpsEph())
                            : droneGps.getFixStatus());
                    gpsStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_not_fixed_grey600_24dp, 0, 0, 0);
                    break;

                case Gps.LOCK_3D:
                    gpsStatus.setText(isHdopEnabled
                            ? String.valueOf(droneGps.getGpsEph())
                            : droneGps.getFixStatus());
                    gpsStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gps_fixed_grey600_24dp, 0, 0, 0);
                    break;
            }
        }
    }

    private void updateHomeStatus() {
        if (droneGps == null || droneHome == null)
            return;

        if (!droneGps.isValid() || !droneHome.isValid())
            return;

        double distanceToHome = MathUtils.getDistance2D(droneHome.getCoordinate(), droneGps.getPosition());
        LengthUnit convertedDistance = UnitManager.getUnitSystem(appPrefs.getUnitSystemType())
                .getLengthUnitProvider().boxBaseValueToTarget(distanceToHome);
        homeStatus.setText(convertedDistance.toString());
    }
}
