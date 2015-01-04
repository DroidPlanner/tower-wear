package com.o3dr.android.dp.wear.fragment;

import android.content.Context;
import android.content.Intent;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Created by fhuya on 1/3/15.
 */
public class FollowMeToggleActionFragment extends BaseActionFragment {
    @Override
    protected int getActionImageResource() {
        final FollowState followState = getVehicleFollowState();
        return followState.isEnabled() ? R.drawable.ic_stop_white_48dp : R.drawable.ic_play_arrow_white_48dp;
    }

    @Override
    protected CharSequence getActionLabel() {
        final FollowState followState = getVehicleFollowState();
        return followState.isEnabled() ? "Stop Follow Me" : "Start Follow Me";
    }

    @Override
    protected void onActionClicked() {
        final Context context = getContext();
        final FollowState followState = getVehicleFollowState();
        final String action = followState.isEnabled() ? WearUtils.ACTION_STOP_FOLLOW_ME : WearUtils
                .ACTION_START_FOLLOW_ME;
        context.startService(new Intent(context, WearReceiverService.class).setAction(action));
    }
}
