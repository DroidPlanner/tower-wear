package com.o3dr.android.dp.wear.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.FlightModesSelectionActivity;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

/**
 * Created by fhuya on 1/3/15.
 */
public class FlightModesActionFragment extends BaseActionFragment {
    @Override
    protected int getActionImageResource() {
        return R.drawable.ic_flight_white_48dp;
    }

    @Override
    protected CharSequence getActionLabel() {
        return getVehicleMode().getLabel();
    }

    private VehicleMode getVehicleMode() {
        VehicleMode vehicleMode = VehicleMode.UNKNOWN;

        State vehicleState = getVehicleState();
        if (vehicleState != null) {
            VehicleMode currentMode = vehicleState.getVehicleMode();
            if (currentMode != null)
                vehicleMode = currentMode;
        }
        return vehicleMode;
    }

    @Override
    protected void onActionClicked() {
        final Context context = getContext();
        context.startActivity(new Intent(context, FlightModesSelectionActivity.class)
                .setAction(AttributeEvent.STATE_VEHICLE_MODE)
                .putExtra(WearReceiverService.EXTRA_EVENT_DATA, (Parcelable) getVehicleMode())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
