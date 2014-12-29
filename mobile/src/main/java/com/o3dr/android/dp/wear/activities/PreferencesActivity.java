package com.o3dr.android.dp.wear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.DroneService;

/**
 * Created by fhuya on 12/27/14.
 */
public class PreferencesActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        startService(new Intent(getApplicationContext(), DroneService.class)
                .setAction(WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getApplicationContext(), DroneService.class));
    }
}
