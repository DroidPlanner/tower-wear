package com.ox3dr.android.wear3pv.fragment;

import android.widget.Toast;

import com.ox3dr.android.wear3pv.R;

/**
 * Created by fhuya on 11/17/14.
 */
public class FollowActionFragment extends BaseActionFragment {
    @Override
    protected int getActionImageResource() {
        return R.drawable.ic_follow;
    }

    @Override
    protected int getActionLabelResource() {
        return R.string.mission_control_follow;
    }

    @Override
    protected void onActionClicked() {
        Toast.makeText(getActivity().getApplicationContext(), "Enabling follow me",
                Toast.LENGTH_LONG).show();
    }
}
