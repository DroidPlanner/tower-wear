package com.o3dr.android.dp.wear.widgets.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.o3dr.android.dp.wear.activities.FollowMeActivity;
import com.o3dr.android.dp.wear.fragment.ConnectActionFragment;
import com.o3dr.android.dp.wear.fragment.ArmActionFragment;
import com.o3dr.android.dp.wear.fragment.DisarmActionFragment;
import com.o3dr.android.dp.wear.fragment.DisconnectActionFragment;
import com.o3dr.android.dp.wear.fragment.FlightModesActionFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeRadiusFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeTypesFragment;
import com.o3dr.android.dp.wear.fragment.FollowMeToggleActionFragment;
import com.o3dr.android.dp.wear.fragment.TakeOffActionFragment;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Pager adapter for the follow me ui actions.
 */
public class ActionPagerAdapter extends FragmentGridPagerAdapter {

    private State vehicleState;
    private final Drawable background;
    private FollowState vehicleFollowState;
    private GuidedState guidedState;

    public ActionPagerAdapter(FragmentManager fm, Drawable background) {
        super(fm);
        this.background = background;
    }

    public void updateVehicleState(State state) {
        this.vehicleState = state;
        notifyDataSetChanged();
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
        switch(row){
            default:
            case 0:
                if(vehicleState == null || !vehicleState.isConnected())
                    fragment = new ConnectActionFragment();
                else{
                    VehicleMode vehicleMode = vehicleState.getVehicleMode();
                    final boolean isCopter = vehicleMode == null || vehicleMode.getDroneType() == Type.TYPE_COPTER;

                    if(isCopter) {
                        if (vehicleState.isFlying()) {
                            fragment = new FlightModesActionFragment();
                        }
                        else if (vehicleState.isArmed()) {
                            if (column == 0) {
                                fragment = new TakeOffActionFragment();
                            } else {
                                fragment = new DisarmActionFragment();
                            }
                        } else {
                            if (column == 0) {
                                fragment = new ArmActionFragment();
                            } else {
                                fragment = new DisconnectActionFragment();
                            }
                        }
                    }
                    else{
                        if(column == 0){
                            fragment = new FlightModesActionFragment();
                        }
                        else{
                            fragment = new DisconnectActionFragment();
                        }
                    }
                }
                break;

            case 1:
                switch(column){
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

        if(fragment != null){
            args.putParcelable(FollowMeActivity.EXTRA_VEHICLE_STATE, vehicleState);
            args.putParcelable(FollowMeActivity.EXTRA_VEHICLE_FOLLOW_STATE, vehicleFollowState);
            args.putParcelable(FollowMeActivity.EXTRA_GUIDED_STATE, guidedState);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public int getRowCount() {
        final boolean isFollowMeReady = vehicleState != null && vehicleState.isConnected() && vehicleState.isArmed() &&
                vehicleState.isFlying() && vehicleFollowState != null && guidedState != null;
        return isFollowMeReady ? 2 : 1;
    }

    @Override
    public int getColumnCount(int row) {
        switch (row) {
            default:
            case 0:
                if (vehicleState == null || !vehicleState.isConnected())
                    return 1;
                else {
                    VehicleMode vehicleMode = vehicleState.getVehicleMode();
                    final boolean isCopter = vehicleMode == null || vehicleMode.getDroneType() == Type.TYPE_COPTER;

                    if(isCopter) {
                        if (vehicleState.isFlying())
                            return 1;
                        else if (vehicleState.isArmed())
                            return 2;
                        else
                            return 2;
                    }
                    else{
                        return 2;
                    }
                }

            case 1:
                return 4;
        }
    }

    @Override
    public Drawable getBackgroundForRow(int row){
        return this.background;
    }
}
