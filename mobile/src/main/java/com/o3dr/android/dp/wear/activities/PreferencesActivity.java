package com.o3dr.android.dp.wear.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.DroneService;

/**
 * Created by fhuya on 12/27/14.
 */
public class PreferencesActivity extends AppCompatActivity {

    private static final IntentFilter intentFilter = new IntentFilter(WearUtils.ACTION_STREAM_NOTIFICATION_SHOWN);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case WearUtils.ACTION_STREAM_NOTIFICATION_SHOWN:
                    Toast.makeText(getApplicationContext(), "Wear app loaded.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onStart(){
        super.onStart();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);

        startService(new Intent(getApplicationContext(), DroneService.class)
                .setAction(WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION));
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }
}
