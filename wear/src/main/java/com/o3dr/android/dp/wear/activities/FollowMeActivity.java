package com.o3dr.android.dp.wear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.ActionPagerAdapter;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.util.ParcelableUtils;

/**
 * Follow me ui implementation.
 */
public class FollowMeActivity extends BaseActivity {

    public static final String EXTRA_VEHICLE_STATE = "extra_vehicle_state";
    public static final String EXTRA_VEHICLE_FOLLOW_STATE = "extra_vehicle_follow_state";
    public static final String EXTRA_GUIDED_STATE = "extra_guided_state";

    private GridViewPager gridView;
    private ActionPagerAdapter actionPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_control);

        actionPagerAdapter = new ActionPagerAdapter(getFragmentManager(),
                getResources().getDrawable(R.drawable.wear_notification_bg));

        gridView = (GridViewPager) findViewById(R.id.grid_view_pager);
        gridView.setAdapter(actionPagerAdapter);

        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION));
    }

    @Override
    public void onStart(){
        super.onStart();
        reloadVehicleData(AttributeType.FOLLOW_STATE);
        reloadVehicleData(AttributeType.GUIDED_STATE);
        reloadVehicleData(AttributeType.STATE);
    }

    @Override
    protected void onVehicleDataUpdated(String dataType, byte[] eventData) {
        switch(dataType){
            case AttributeType.STATE:
                State vehicleState = eventData == null ? null : ParcelableUtils.unmarshall(eventData, State.CREATOR);
                final boolean isConnected = vehicleState != null && vehicleState.isConnected();
                actionPagerAdapter.updateVehicleState(vehicleState);
                gridView.setKeepScreenOn(isConnected);
                break;

            case AttributeType.FOLLOW_STATE:
                FollowState vehicleFollowState = eventData == null ? null : ParcelableUtils.unmarshall(eventData,
                        FollowState.CREATOR);
                actionPagerAdapter.updateFollowState(vehicleFollowState);
                break;

            case AttributeType.GUIDED_STATE:
                GuidedState guidedState = eventData == null ? null : ParcelableUtils.unmarshall(eventData,
                        GuidedState.CREATOR);
                actionPagerAdapter.updateGuidedState(guidedState);
                break;
        }
    }
}
