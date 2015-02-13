package com.o3dr.android.dp.wear.services;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.o3dr.android.dp.wear.activities.PreferencesActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.util.ParcelableUtils;

/**
 * Created by fhuya on 12/30/14.
 */
public class WearListenerService extends WearRelayService {

    private static final String TAG = WearListenerService.class.getSimpleName();

    @Override
    protected void onActionRequested(String actionPath, byte[] data) {
        Log.d(TAG, "Action requested: " + actionPath);
        switch (actionPath) {
            case WearUtils.ACTION_CONNECT:
            case WearUtils.ACTION_DISCONNECT:
            case WearUtils.ACTION_ARM:
            case WearUtils.ACTION_DISARM:
            case WearUtils.ACTION_TAKE_OFF:
            case WearUtils.ACTION_START_FOLLOW_ME:
            case WearUtils.ACTION_STOP_FOLLOW_ME:
            case WearUtils.ACTION_STREAM_NOTIFICATION_SHOWN:
                startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath));
                break;

            case WearUtils.ACTION_CHANGE_VEHICLE_MODE:
                if (data != null) {
                    VehicleMode vehicleMode = ParcelableUtils.unmarshall(data, VehicleMode.CREATOR);
                    if (vehicleMode != null) {
                        startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath)
                                .putExtra(DroneService.EXTRA_ACTION_DATA, (Parcelable) vehicleMode));
                    }
                }
                break;

            case WearUtils.ACTION_CHANGE_FOLLOW_ME_TYPE:
                if (data != null) {
                    FollowType followType = ParcelableUtils.unmarshall(data, FollowType.CREATOR);
                    if (followType != null) {
                        startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath)
                                .putExtra(DroneService.EXTRA_ACTION_DATA, (Parcelable) followType));
                    }
                }
                break;

            case WearUtils.ACTION_SET_FOLLOW_ME_RADIUS:
            case WearUtils.ACTION_SET_GUIDED_ALTITUDE:
                if (data != null) {
                    final int radius = data[0];
                    startService(new Intent(getApplicationContext(), DroneService.class).setAction(actionPath)
                            .putExtra(DroneService.EXTRA_ACTION_DATA, radius));
                }
                break;

            case WearUtils.ACTION_OPEN_PHONE_APP:
                startActivity(new Intent(getApplicationContext(), PreferencesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "Connected to wear node " + peer.getDisplayName());
        startService(new Intent(getApplicationContext(), DroneService.class)
                .setAction(WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION));
    }

    @Override
    public void onPeerDisconnected(Node peer){
        //If connected, disconnect.
        startService(new Intent(getApplicationContext(), DroneService.class).setAction(WearUtils.ACTION_DISCONNECT));
    }
}
