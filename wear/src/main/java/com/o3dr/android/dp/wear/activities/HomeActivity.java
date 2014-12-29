package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;

/**
 * Created by fhuya on 12/28/14.
 */
public class HomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION));
    }
}
