package com.o3dr.android.dp.wear.fragment;

import android.content.Intent;
import android.os.Parcelable;


import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.DriftControlActivity;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

/**
 * Created by Toby on 8/9/2015.
 */
public class DriftControlActionFragment extends BaseActionFragment {
    @Override
    protected int getActionImageResource() {
        return R.drawable.ic_control_white_48dp;
    }

    @Override
    protected CharSequence getActionLabel() {
        return "Drift Control";
    }

    @Override
    protected void onActionClicked() {
        getActivity().startService(new Intent(getContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_CHANGE_VEHICLE_MODE)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, (Parcelable) VehicleMode.COPTER_GUIDED));
        getActivity().startActivity(new Intent(getContext(), DriftControlActivity.class));
    }
}
