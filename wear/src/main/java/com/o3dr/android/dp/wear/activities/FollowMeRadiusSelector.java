package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.fragment.FollowMeRadiusFragment;
import com.o3dr.android.dp.wear.lib.utils.AppPreferences;
import com.o3dr.android.dp.wear.lib.utils.WearFollowState;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.lib.utils.unit.UnitManager;
import com.o3dr.android.dp.wear.lib.utils.unit.providers.length.LengthUnitProvider;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.FollowMeRadiusAdapter;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.beyene.sius.unit.length.LengthUnit;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowMeRadiusSelector extends Activity implements WearableListView.ClickListener {

    private static final IntentFilter intentFilter = new IntentFilter(WearUtils.ACTION_PREFERENCES_UPDATED);
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case WearUtils.ACTION_PREFERENCES_UPDATED:
                    final int prefKeyId = intent.getIntExtra(WearUtils.EXTRA_PREFERENCE_KEY_ID, -1);
                    switch(prefKeyId){
                        case R.string.pref_unit_system_key:
                            if(followRadiusAdapter != null) {
                                final LengthUnitProvider lengthUnitProvider = UnitManager
                                        .getUnitSystem(appPrefs.getUnitSystemType()).getLengthUnitProvider();

                                followRadiusAdapter.setLengthUnitProvider(lengthUnitProvider);
                            }
                            break;
                    }
                    break;
            }
        }
    };

    private WearableListView radiusSelectorView;
    private int radiusType;
    private FollowMeRadiusAdapter followRadiusAdapter;
    private AppPreferences appPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_list_view);

        appPrefs = new AppPreferences(getApplicationContext());

        followRadiusAdapter = new FollowMeRadiusAdapter(UnitManager.getUnitSystem(appPrefs.getUnitSystemType())
                .getLengthUnitProvider());

        radiusSelectorView = (WearableListView) findViewById(R.id.wear_list);
        radiusSelectorView.setAdapter(followRadiusAdapter);
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
                WearFollowState followState = intent.getParcelableExtra(WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE);
                currentRadius = (int) followState.getRadius();
                break;

            case FollowMeRadiusFragment.VERTICAL_RADIUS_TYPE:
            default:
                GuidedState guidedState = intent.getParcelableExtra(WearUIActivity.EXTRA_GUIDED_STATE);
                currentRadius = (int) guidedState.getCoordinate().getAltitude();
                break;
        }

        final int radiusPosition = Math.max(0, currentRadius - FollowMeRadiusAdapter.MIN_RADIUS);
        updateSelectorPosition(UnitManager.getUnitSystem(appPrefs.getUnitSystemType()).getLengthUnitProvider(),
                radiusPosition);
    }

    private void updateSelectorPosition(LengthUnitProvider lengthUnitProvider, int rawPosition){
        LengthUnit convertedPosition = lengthUnitProvider.boxBaseValueToTarget(rawPosition);

        RecyclerView.LayoutManager layoutMgr = radiusSelectorView.getLayoutManager();
        if (layoutMgr != null) {
            layoutMgr.scrollToPosition((int) Math.round(convertedPosition.getValue()));
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final String action = radiusType == FollowMeRadiusFragment.VERTICAL_RADIUS_TYPE
                ? WearUtils.ACTION_SET_GUIDED_ALTITUDE
                : WearUtils.ACTION_SET_FOLLOW_ME_RADIUS;

        final LengthUnit radius = (LengthUnit) viewHolder.itemView.getTag();
        final int convertedRadius = (int) Math.round(radius.toBase().getValue());
        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(action)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, convertedRadius));

        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {    }

    @Override
    public void onStart(){
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }
}
