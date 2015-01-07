package com.o3dr.android.dp.wear.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.WearUIActivity;
import com.o3dr.android.dp.wear.activities.FollowMeRadiusSelector;

/**
 * Created by fhuya on 1/4/15.
 */
public class FollowMeRadiusFragment extends BaseActionFragment {

    public static final int VERTICAL_RADIUS_TYPE = 0;
    public static final int HORIZONTAL_RADIUS_TYPE = 1;

    public static final String EXTRA_RADIUS_TYPE = "extra_radius_type";

    private int radiusType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        radiusType = arguments.getInt(EXTRA_RADIUS_TYPE, VERTICAL_RADIUS_TYPE);
    }

    @Override
    protected int getActionImageResource() {
        if (radiusType == HORIZONTAL_RADIUS_TYPE)
            return R.drawable.ic_settings_ethernet_white_48dp;
        else
            return R.drawable.ic_format_line_spacing_white_48dp;
    }

    @Override
    protected CharSequence getActionLabel() {
        if (radiusType == HORIZONTAL_RADIUS_TYPE) {
            return "Follow Me Radius";
        } else {
            return "Follow Me Altitude";
        }
    }

    @Override
    protected void onActionClicked() {
        final String dataKey = radiusType == HORIZONTAL_RADIUS_TYPE
                ? WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE
                : WearUIActivity.EXTRA_GUIDED_STATE;
        final Parcelable data = radiusType == HORIZONTAL_RADIUS_TYPE
                ? getVehicleFollowState()
                : getVehicleGuidedState();

        final Context context = getContext();
        context.startActivity(new Intent(context, FollowMeRadiusSelector.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(EXTRA_RADIUS_TYPE, radiusType)
                .putExtra(dataKey, data));
    }
}
