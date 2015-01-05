package com.o3dr.android.dp.wear.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.FollowMeActivity;
import com.o3dr.android.dp.wear.activities.FollowMeTypesSelector;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.FollowTypesAdapter;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

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
                .putExtra(FollowMeActivity.EXTRA_VEHICLE_FOLLOW_STATE, followState));
    }
}
