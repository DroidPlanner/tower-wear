package com.o3dr.android.dp.wear.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.o3dr.android.dp.wear.R;

/**
 * Used to prompt the user to install the Android Wear app.
 */
public class InstallAndroidWearApp extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_android_wear_dialog);

        final Button cancelButton = (Button) findViewById(R.id.dialog_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Button installButton = (Button) findViewById(R.id.dialog_install_button);
        installButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.google.android.wearable.app")));
                }catch(ActivityNotFoundException e){
                    Toast.makeText(getApplicationContext(), "No activity found to handle the install request!",
                            Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });
    }
}
