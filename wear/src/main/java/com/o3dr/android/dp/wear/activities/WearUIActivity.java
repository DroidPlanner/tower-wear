package com.o3dr.android.dp.wear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.ActionPagerAdapter;
import com.o3dr.android.dp.wear.widgets.indicators.CirclePageIndicator;
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

    private int currentRow = 0;

    private PageIndicator horizontalPageIndicator;
    private PageIndicator verticalPageIndicator;

    private GridViewPager gridView;
    private ActionPagerAdapter actionPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_control);

        actionPagerAdapter = new ActionPagerAdapter(getFragmentManager());

        verticalPageIndicator = (PageIndicator) findViewById(R.id.vertical_page_indicator);
        verticalPageIndicator.setPageCount(actionPagerAdapter.getRowCount());

        horizontalPageIndicator = (PageIndicator) findViewById(R.id.horizontal_page_indicator);
        horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));

        gridView = (GridViewPager) findViewById(R.id.grid_view_pager);
        gridView.setOnAdapterChangeListener(this);
        gridView.setOnPageChangeListener(this);
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

    @Override
    public void onPageScrolled(int row, int column, float rowOffset, float columnOffset, int rowOffsetPixels,
                               int columnOffsetPixels) {
        verticalPageIndicator.onPageScrolled(row, rowOffset, rowOffsetPixels);

        if(row != currentRow){
            currentRow = row;
            horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));
        }

        horizontalPageIndicator.onPageScrolled(column, columnOffset, columnOffsetPixels);
    }

    @Override
    public void onPageSelected(int row, int column) {
        verticalPageIndicator.onPageSelected(row);

        if(row != currentRow){
            currentRow = row;
            horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));
        }
        horizontalPageIndicator.onPageSelected(column);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        verticalPageIndicator.onPageScrollStateChanged(state);
        horizontalPageIndicator.onPageScrollStateChanged(state);
    }

    @Override
    public void onAdapterChanged(GridPagerAdapter oldAdapter, GridPagerAdapter newAdapter) { }

    @Override
    public void onDataSetChanged() {
        currentRow = 0;
        verticalPageIndicator.setPageCount(actionPagerAdapter.getRowCount());
        horizontalPageIndicator.setPageCount(actionPagerAdapter.getColumnCount(currentRow));
    }
}
