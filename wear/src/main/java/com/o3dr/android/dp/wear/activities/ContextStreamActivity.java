package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.SpannableUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.property.State;

/**
 * Created by fhuya on 12/28/14.
 */
public class ContextStreamActivity extends Activity {

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.BATTERY_UPDATED);
        eventFilter.addAction(AttributeEvent.SIGNAL_UPDATED);
        eventFilter.addAction(AttributeEvent.GPS_FIX);
        eventFilter.addAction(AttributeEvent.GPS_COUNT);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch(action){
                case AttributeEvent.STATE_CONNECTED:
                    break;
            }
        }
    };

    private TextView signalStatus;
    private TextView batteryStatus;
    private ImageView gpsStatus;
    private TextView connectionStatus;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_stream);

        signalStatus = (TextView) findViewById(R.id.bar_signal);
        batteryStatus = (TextView) findViewById(R.id.bar_battery);
        gpsStatus = (ImageView) findViewById(R.id.bar_gps);
        connectionStatus = (TextView) findViewById(R.id.connection_status);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(intent == null)
            return;

        State vehicleState = intent.getParcelableExtra(WearReceiverService.EXTRA_VEHICLE_STATE);
        final CharSequence connectionLabel = vehicleState != null && vehicleState.isConnected()
                ? SpannableUtils.color(Color.GREEN, "connected")
                : SpannableUtils.color(Color.RED, "disconnected");
        connectionStatus.setText(SpannableUtils.normal("DP Wear: ", connectionLabel));
    }

    @Override
    public void onStart(){
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(eventReceiver);
    }
}
