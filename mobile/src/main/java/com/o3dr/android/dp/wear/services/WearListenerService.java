package com.o3dr.android.dp.wear.services;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.o3dr.android.dp.wear.activities.PreferencesActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.util.ParcelableUtils;

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
            case WearUtils.ACTION_ARM:
            case WearUtils.ACTION_DISARM:
            case WearUtils.ACTION_TAKE_OFF:
                startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath));
                break;

            case WearUtils.ACTION_CHANGE_VEHICLE_MODE:
                if(data != null){
                    VehicleMode vehicleMode = ParcelableUtils.unmarshall(data, VehicleMode.CREATOR);
                    if(vehicleMode != null){
                        startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath)
                                .putExtra(DroneService.EXTRA_ACTION_DATA, (Parcelable) vehicleMode));
                    }
                }
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
