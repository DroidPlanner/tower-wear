package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.FollowTypesAdapter;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowMeTypesSelector extends Activity implements WearableListView.ClickListener {

    private WearableListView followTypesView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_list_view);

        followTypesView = (WearableListView) findViewById(R.id.wear_list);
        followTypesView.setAdapter(new FollowTypesAdapter());
        followTypesView.setClickListener(this);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null)
            return;

        final FollowState followState = intent.getParcelableExtra(WearUIActivity.EXTRA_VEHICLE_FOLLOW_STATE);
        if (followState != null) {
            RecyclerView.LayoutManager layoutMgr = followTypesView.getLayoutManager();
            if (layoutMgr != null) {
                layoutMgr.scrollToPosition(followState.getMode().ordinal());
            }
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final FollowType followType = (FollowType) viewHolder.itemView.getTag();
        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_CHANGE_FOLLOW_ME_TYPE)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, (Parcelable) followType));

        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
