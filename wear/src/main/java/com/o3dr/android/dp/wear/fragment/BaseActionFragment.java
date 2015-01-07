package com.o3dr.android.dp.wear.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.WearUIActivity;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Template for the action views used in the app.
 */
public abstract class BaseActionFragment extends Fragment implements View.OnClickListener {

    private State vehicleState;
    private FollowState vehicleFollowState;
    private GuidedState guidedState;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if(arguments != null) {
            vehicleState = arguments.getParcelable(WearUIActivity.EXTRA_VEHICLE_STATE);
            vehicleFollowState = arguments.getParcelable(WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE);
            guidedState = arguments.getParcelable(WearUIActivity.EXTRA_GUIDED_STATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final CircledImageView faveImage = (CircledImageView) view.findViewById(R.id.listing_action_image);
        if (faveImage != null) {
            faveImage.setImageResource(getActionImageResource());
            faveImage.setOnClickListener(this);
        }

        final TextView faveLabel = (TextView) view.findViewById(R.id.listing_action_label);
        if (faveLabel != null) {
            faveLabel.setText(getActionLabel());
        }
    }

    protected Context getContext(){
        return getActivity().getApplicationContext();
    }

    protected State getVehicleState(){
        return vehicleState;
    }

    protected FollowState getVehicleFollowState(){
        return vehicleFollowState;
    }

    protected GuidedState getVehicleGuidedState(){
        return guidedState;
    }

    protected abstract int getActionImageResource();

    protected abstract CharSequence getActionLabel();

    protected abstract void onActionClicked();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.listing_action_image:
                onActionClicked();
                break;
        }
    }

}
