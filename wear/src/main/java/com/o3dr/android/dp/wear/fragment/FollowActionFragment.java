package com.o3dr.android.dp.wear.fragment;

import com.o3dr.android.client.Drone;
import com.o3dr.android.dp.wear.R;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

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
        Drone drone = getDrone();
        if(drone == null){
            showUser("Drone handle is not available yet.");
            return;
        }

        if(drone.isConnected()){
            drone.enableFollowMe(FollowType.LEASH);
            showUser("Starting follow-me...");
        }
        else{
            showUser("No drone connection available.");
        }
    }
}
