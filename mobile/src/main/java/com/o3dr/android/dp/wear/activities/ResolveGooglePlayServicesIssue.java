package com.o3dr.android.dp.wear.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.services.DroneService;

/**
 * Used to prompt the user to fix the google play services issue.
 */
public class ResolveGooglePlayServicesIssue extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_play_resolution);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if(intent == null)
            return;

        final int errorCode = intent.getIntExtra(DroneService.EXTRA_ERROR_CODE, ConnectionResult.SUCCESS);
        if(errorCode != ConnectionResult.SUCCESS)
            GooglePlayServicesUtil.showErrorDialogFragment(errorCode, this, 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
    }

    @Override
    public void onStop(){
        super.onStop();
        finish();
    }
}
