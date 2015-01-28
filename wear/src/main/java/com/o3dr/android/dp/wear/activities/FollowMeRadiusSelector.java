package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.fragment.FollowMeRadiusFragment;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.FollowMeRadiusAdapter;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowMeRadiusSelector extends Activity implements WearableListView.ClickListener {

    private WearableListView radiusSelectorView;
    private int radiusType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_list_view);

        radiusSelectorView = (WearableListView) findViewById(R.id.wear_list);
        radiusSelectorView.setAdapter(new FollowMeRadiusAdapter());
        radiusSelectorView.setClickListener(this);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(intent == null)
            return;

        radiusType = intent.getIntExtra(FollowMeRadiusFragment.EXTRA_RADIUS_TYPE, FollowMeRadiusFragment.VERTICAL_RADIUS_TYPE);
        final int currentRadius;
        switch (radiusType) {
            case FollowMeRadiusFragment.HORIZONTAL_RADIUS_TYPE:
                FollowState followState = intent.getParcelableExtra(WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE);
                Bundle params = followState.getParams();
                currentRadius = (int) params.getDouble(FollowType.EXTRA_FOLLOW_RADIUS, 2);
                break;

            case FollowMeRadiusFragment.VERTICAL_RADIUS_TYPE:
            default:
                GuidedState guidedState = intent.getParcelableExtra(WearUIActivity.EXTRA_GUIDED_STATE);
                currentRadius = (int) guidedState.getCoordinate().getAltitude();
                break;
        }

        RecyclerView.LayoutManager layoutMgr = radiusSelectorView.getLayoutManager();
        if (layoutMgr != null) {
            layoutMgr.scrollToPosition(currentRadius);
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final String action = radiusType == FollowMeRadiusFragment.VERTICAL_RADIUS_TYPE
                ? WearUtils.ACTION_SET_GUIDED_ALTITUDE
                : WearUtils.ACTION_SET_FOLLOW_ME_RADIUS;
        final Integer radius = (Integer) viewHolder.itemView.getTag();
        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(action)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, radius));

        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
