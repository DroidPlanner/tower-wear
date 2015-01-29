package com.o3dr.android.dp.wear.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearFollowState;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.ActionPagerAdapter;
import com.o3dr.android.dp.wear.widgets.indicators.PageIndicator;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.util.ParcelableUtils;

/**
 * Follow me ui implementation.
 */
public class WearUIActivity extends BaseActivity implements GridViewPager.OnPageChangeListener, GridViewPager.OnAdapterChangeListener {

    public static final String EXTRA_VEHICLE_STATE = "extra_vehicle_state";
    public static final String EXTRA_VEHICLE_FOLLOW_STATE = "extra_vehicle_follow_state";
    public static final String EXTRA_GUIDED_STATE = "extra_guided_state";

    private final static IntentFilter intentFilter = new IntentFilter(WearUtils.ACTION_PREFERENCES_UPDATED);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case WearUtils.ACTION_PREFERENCES_UPDATED:
                    int prefKeyId = intent.getIntExtra(WearUtils.EXTRA_PREFERENCE_KEY_ID, -1);
                    switch(prefKeyId){
                        case R.string.pref_keep_screen_bright_key:
                            if(gridView != null)
                                gridView.setKeepScreenOn(appPrefs.keepScreenBright());
                            break;
                    }
                    break;
            }
        }
    };

    private int currentRow = 0;

    private PageIndicator horizontalPageIndicator;

    private GridViewPager gridView;
    private ActionPagerAdapter actionPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_control);

        actionPagerAdapter = new ActionPagerAdapter(getFragmentManager());

        horizontalPageIndicator = (PageIndicator) findViewById(R.id.horizontal_page_indicator);
        horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));

        gridView = (GridViewPager) findViewById(R.id.grid_view_pager);
        gridView.setOnAdapterChangeListener(this);
        gridView.setOnPageChangeListener(this);
        gridView.setAdapter(actionPagerAdapter);

        reloadVehicleData(AttributeType.STATE);
        reloadVehicleData(AttributeType.FOLLOW_STATE);
        reloadVehicleData(AttributeType.GUIDED_STATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onVehicleDataUpdated(String dataType, byte[] eventData) {
        switch (dataType) {
            case AttributeType.STATE:
                State vehicleState = eventData == null ? null : ParcelableUtils.unmarshall(eventData, State.CREATOR);
                final boolean isFollowMeReady = vehicleState != null && vehicleState.isConnected()
                        && vehicleState.isArmed() && vehicleState.isFlying();
                if (!isFollowMeReady)
                    finish();
                else {
                    gridView.setKeepScreenOn(appPrefs.keepScreenBright());
                }
                break;

            case AttributeType.FOLLOW_STATE:
                WearFollowState vehicleFollowState = eventData == null ? null : ParcelableUtils.unmarshall(eventData,
                        WearFollowState.CREATOR);
                actionPagerAdapter.updateFollowState(vehicleFollowState);
                break;

            case AttributeType.GUIDED_STATE:
                GuidedState guidedState = eventData == null ? null : ParcelableUtils.unmarshall(eventData,
                        GuidedState.CREATOR);
                actionPagerAdapter.updateGuidedState(guidedState);
                break;
        }
    }

    @Override
    public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels,
                               int columnOffsetPixels) {
        if (row != currentRow) {
            currentRow = row;
            horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));
        }

        horizontalPageIndicator.onPageScrolled(column, columnOffset, columnOffsetPixels);
    }

    @Override
    public void onPageSelected(int row, int column) {
        if (row != currentRow) {
            currentRow = row;
            horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));
        }
        horizontalPageIndicator.onPageSelected(column);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        horizontalPageIndicator.onPageScrollStateChanged(state);
    }

    @Override
    public void onAdapterChanged(GridPagerAdapter oldAdapter, GridPagerAdapter newAdapter) {
    }

    @Override
    public void onDataSetChanged() {
        currentRow = 0;
        horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));
    }
}
