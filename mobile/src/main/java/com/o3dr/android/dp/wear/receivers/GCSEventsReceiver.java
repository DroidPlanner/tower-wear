package com.o3dr.android.dp.wear.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.DroneService;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;

/**
 * Created by Fredia Huya-Kouadio on 1/17/15.
 */
public class GCSEventsReceiver extends BroadcastReceiver {

    private static final String TAG = GCSEventsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        String appId = intent.getStringExtra(GCSEvent.EXTRA_APP_ID);
        if (WearUtils.TOWER_APP_ID.equals(appId)) {
            switch (action) {
                case GCSEvent.ACTION_VEHICLE_CONNECTION:
                    Log.d(TAG, "Received vehicle connection event.");

                    ConnectionParameter connParams = intent.getParcelableExtra(GCSEvent
                            .EXTRA_VEHICLE_CONNECTION_PARAMETER);
                    context.startService(new Intent(context, DroneService.class)
                            .setAction(WearUtils.ACTION_CONNECT)
                            .putExtra(DroneService.EXTRA_CONNECTION_PARAMETER, connParams));
                    break;

                case GCSEvent.ACTION_VEHICLE_DISCONNECTION:
                    Log.d(TAG, "Received vehicle disconnection event.");

                    context.startService(new Intent(context, DroneService.class)
                            .setAction(WearUtils.ACTION_DISCONNECT));
                    break;
            }
        }
    }
}
