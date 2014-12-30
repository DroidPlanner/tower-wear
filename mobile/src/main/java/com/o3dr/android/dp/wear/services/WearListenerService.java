package com.o3dr.android.dp.wear.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.o3dr.android.dp.wear.activities.PreferencesActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;

/**
 * Created by fhuya on 12/30/14.
 */
public class WearListenerService extends WearRelayService {

    private static final String TAG = WearListenerService.class.getSimpleName();

    @Override
    protected void onActionRequested(String actionPath, byte[] data){
        Log.d(TAG, "Action requested: " + actionPath);
        switch(actionPath){
            case WearUtils.ACTION_CONNECT:
            case WearUtils.ACTION_DISCONNECT:
                startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath));
                break;

            case WearUtils.ACTION_REQUEST_ATTRIBUTE:
                //Retrieve the attribute to request from the data argument.

                break;

            case WearUtils.ACTION_OPEN_PHONE_APP:
                startActivity(new Intent(getApplicationContext(), PreferencesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }

    @Override
    public void onPeerConnected(Node peer){
        Log.d(TAG, "Connected to wear node " + peer.getDisplayName());
    }
}
