package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.DataEventBuffer;
import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.SpannableUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.util.MathUtils;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import java.util.Locale;

/**
 * Created by fhuya on 12/28/14.
 */
public class ContextStreamActivity extends BaseActivity {

    private final static String TAG = ContextStreamActivity.class.getSimpleName();

    private View activityLayout;
    private TextView signalStatus;
    private TextView batteryStatus;
    private ImageView gpsStatus;
    private TextView connectionStatus;

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "Creating context stream activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_stream);

        activityLayout = findViewById(R.id.context_stream_layout);
        signalStatus = (TextView) findViewById(R.id.bar_signal);
        batteryStatus = (TextView) findViewById(R.id.bar_battery);
        gpsStatus = (ImageView) findViewById(R.id.bar_gps);
        connectionStatus = (TextView) findViewById(R.id.connection_status);

        onEventReceived(getIntent());
    }

    @Override
    public void onStart(){
        super.onStart();
        reloadVehicleData(AttributeType.STATE);
        reloadVehicleData(AttributeType.GPS);
        reloadVehicleData(AttributeType.BATTERY);
        reloadVehicleData(AttributeType.SIGNAL);
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        onEventReceived(intent);
    }

    private void onEventReceived(Intent intent){
        if(intent == null)
            return;

        final String action = intent.getAction();
        if(action == null)
            return;

        final byte[] eventData = intent.getByteArrayExtra(WearReceiverService.EXTRA_EVENT_DATA);
        onVehicleDataUpdated(action, eventData);
    }

    @Override
    protected void onVehicleDataUpdated(String dataType, byte[] eventData) {
        switch(dataType){
            case AttributeType.STATE:
                State vehicleState = eventData == null ? null : ParcelableUtils.unmarshall(eventData, State.CREATOR);
                final boolean isConnected = vehicleState != null && vehicleState.isConnected();
                activityLayout.setKeepScreenOn(isConnected);
                final CharSequence connectionLabel = isConnected
                        ? SpannableUtils.color(Color.GREEN, "connected")
                        : SpannableUtils.color(Color.RED, "disconnected");
                connectionStatus.setText(SpannableUtils.normal("DP Wear: ", connectionLabel));
                break;

            case AttributeType.GPS:
                Gps gps = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Gps.CREATOR);
                if(gps == null){
                    gpsStatus.setImageResource(R.drawable.ic_gps_off_black_24dp);
                }
                else{
                    switch(gps.getFixStatus()){
                        case Gps.NO_FIX:
                        case Gps.LOCK_2D:
                            gpsStatus.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp);
                            break;

                        case Gps.LOCK_3D:
                            gpsStatus.setImageResource(R.drawable.ic_gps_fixed_black_24dp);
                            break;
                    }
                }
                break;

            case AttributeType.BATTERY:
                Battery battery = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Battery.CREATOR);
                if(battery == null)
                    batteryStatus.setText(R.string.empty_content);
                else {
                    batteryStatus.setText(String.format(Locale.ENGLISH, "%2.1fv", battery.getBatteryVoltage()));
                }
                break;

            case AttributeType.SIGNAL:
                Signal signal = eventData == null ? null : ParcelableUtils.unmarshall(eventData, Signal.CREATOR);
                if(signal == null || !signal.isValid()){
                    signalStatus.setText(R.string.empty_content);
                }
                else{
                    final int signalStrength = MathUtils.getSignalStrength(signal.getFadeMargin(),
                            signal.getRemFadeMargin());
                    signalStatus.setText(signalStrength + "%");
                }
                break;
        }
    }


}
