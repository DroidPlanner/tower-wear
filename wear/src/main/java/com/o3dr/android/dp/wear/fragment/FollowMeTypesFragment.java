package com.o3dr.android.dp.wear.fragment;

import android.content.Context;
import android.content.Intent;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.WearUIActivity;
import com.o3dr.android.dp.wear.activities.FollowMeTypesSelector;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Created by fhuya on 1/3/15.
 */
public class FollowMeTypesFragment extends BaseActionFragment {

    @Override
    protected int getActionImageResource() {
        return R.drawable.ic_list_white_48dp;
    }

    @Override
    protected CharSequence getActionLabel() {
        return getText(R.string.label_follow_me_types);
    }

    @Override
    protected void onActionClicked() {
        final Context context = getContext();
        final FollowState followState = getVehicleFollowState();
        startActivity(new Intent(context, FollowMeTypesSelector.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE, followState));
    }
}
