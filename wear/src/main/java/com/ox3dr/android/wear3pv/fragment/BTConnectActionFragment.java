package com.ox3dr.android.wear3pv.fragment;

import android.widget.Toast;

import com.ox3dr.android.wear3pv.R;

/**
 * Created by fhuya on 11/17/14.
 */
public class BTConnectActionFragment extends BaseActionFragment {
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
        Toast.makeText(getActivity().getApplicationContext(), "Connecting...",
                Toast.LENGTH_LONG).show();
    }
}
