package com.ox3dr.android.wear3pv.fragment;

import android.os.Bundle;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.connection.StreamRates;
import com.ox3dr.android.wear3pv.R;

/**
 * Created by fhuya on 11/17/14.
 */
public class BTConnectActionFragment extends BaseActionFragment {

    private static final String BLUETOOTH_ADDRESS = "30:14:09:02:22:45";

    @Override
    protected int getActionImageResource() {
        return android.R.drawable.stat_sys_data_bluetooth;
    }

    @Override
    protected int getActionLabelResource() {
        return R.string.connect;
    }

    @Override
    protected void onActionClicked() {
        Drone drone = getDrone();
        if(drone == null){
            showUser("Drone handle is not available yet.");
            return;
        }

        final Bundle extraParam = new Bundle(1);
        extraParam.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, BLUETOOTH_ADDRESS);

        final StreamRates rates = new StreamRates();
        rates.setExtendedStatus(2);
        rates.setExtra1(10);
        rates.setExtra2(2);
        rates.setExtra3(2);
        rates.setPosition(3);
        rates.setRcChannels(5);
        rates.setRawSensors(2);
        rates.setRawController(3);

        final DroneSharePrefs droneSharePrefs = new DroneSharePrefs("", "", false, false);

        final ConnectionParameter connParam = new ConnectionParameter(ConnectionType
                .TYPE_BLUETOOTH, extraParam, rates, droneSharePrefs);

        drone.connect(connParam);
        showUser("Connecting...");
    }


}
