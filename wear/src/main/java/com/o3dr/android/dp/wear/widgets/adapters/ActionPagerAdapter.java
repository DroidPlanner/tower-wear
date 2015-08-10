package com.o3dr.android.dp.wear.widgets.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.o3dr.android.dp.wear.activities.DriftControlActivity;
import com.o3dr.android.dp.wear.activities.WearUIActivity;
import com.o3dr.android.dp.wear.fragment.DriftControlActionFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeRadiusFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeToggleActionFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeTypesFragment;
import com.o3dr.android.dp.wear.lib.utils.WearFollowState;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

/**
 * Pager adapter for the follow me ui actions.
 */
public class ActionPagerAdapter extends FragmentGridPagerAdapter {

    public ActionPagerAdapter(FragmentManager fm) {
        super(fm);
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
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int row) {
        return 4;
    }
}
