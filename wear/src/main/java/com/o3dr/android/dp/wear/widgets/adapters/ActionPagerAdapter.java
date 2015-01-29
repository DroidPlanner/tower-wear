package com.o3dr.android.dp.wear.widgets.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.o3dr.android.dp.wear.activities.WearUIActivity;
import com.o3dr.android.dp.wear.fragment.FollowMeRadiusFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeToggleActionFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeTypesFragment;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

/**
 * Pager adapter for the follow me ui actions.
 */
public class ActionPagerAdapter extends FragmentGridPagerAdapter {

    private FollowState vehicleFollowState;
    private GuidedState guidedState;

    public ActionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void updateFollowState(FollowState vehicleFollowState) {
        this.vehicleFollowState = vehicleFollowState;
        notifyDataSetChanged();
    }

    public void updateGuidedState(GuidedState guidedState) {
        this.guidedState = guidedState;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getFragment(int row, int column) {
        Fragment fragment;
        final Bundle args = new Bundle();
        switch (row) {
            default:
                switch (column) {
                    default:
                    case 0:
                        fragment = new FollowMeToggleActionFragment();
                        break;

                    case 1:
                        fragment = new FollowMeTypesFragment();
                        break;

                    case 2:
                        fragment = new FollowMeRadiusFragment();
                        args.putInt(FollowMeRadiusFragment.EXTRA_RADIUS_TYPE, FollowMeRadiusFragment.VERTICAL_RADIUS_TYPE);
                        break;

                    case 3:
                        fragment = new FollowMeRadiusFragment();
                        args.putInt(FollowMeRadiusFragment.EXTRA_RADIUS_TYPE,
                                FollowMeRadiusFragment.HORIZONTAL_RADIUS_TYPE);
                        break;
                }
                break;
        }

        if (fragment != null) {
            args.putParcelable(WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE, vehicleFollowState);
            args.putParcelable(WearUIActivity.EXTRA_GUIDED_STATE, guidedState);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public int getRowCount() {
        return vehicleFollowState ==  null ? 0 : 1;
    }

    @Override
    public int getColumnCount(int row) {
        if(vehicleFollowState != null){
            FollowType followType = vehicleFollowState.getMode();
            if(followType.hasParam(FollowType.EXTRA_FOLLOW_RADIUS))
                return 4;
            else
                return 3;
        }

        return 4;
    }
}
