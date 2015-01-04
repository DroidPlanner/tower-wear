package com.o3dr.android.dp.wear.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.FollowMeActivity;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.FollowMeRadiusAdapter;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Created by fhuya on 1/4/15.
 */
public class FollowMeRadiusFragment extends Fragment implements WearableListView.ClickListener {

    public static final int VERTICAL_RADIUS_TYPE = 0;
    public static final int HORIZONTAL_RADIUS_TYPE = 1;

    public static final String EXTRA_RADIUS_TYPE = "extra_radius_type";

    private int radiusType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wear_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WearableListView radiusSelectorView = (WearableListView) view.findViewById(R.id.wear_list);
        radiusSelectorView.setAdapter(new FollowMeRadiusAdapter());
        radiusSelectorView.setClickListener(this);

        final Bundle arguments = getArguments();
        radiusType = arguments.getInt(EXTRA_RADIUS_TYPE, VERTICAL_RADIUS_TYPE);
        final int currentRadius;
        switch (radiusType) {
            case HORIZONTAL_RADIUS_TYPE:
                FollowState followState = arguments.getParcelable(FollowMeActivity.EXTRA_VEHICLE_FOLLOW_STATE);
                currentRadius = (int) followState.getRadius();
                break;

            case VERTICAL_RADIUS_TYPE:
            default:
                GuidedState guidedState = arguments.getParcelable(FollowMeActivity.EXTRA_GUIDED_STATE);
                currentRadius = (int) guidedState.getCoordinate().getAltitude();
                break;
        }

        RecyclerView.LayoutManager layoutMgr = radiusSelectorView.getLayoutManager();
        if(layoutMgr != null){
            layoutMgr.scrollToPosition(currentRadius);
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        final String action = radiusType == VERTICAL_RADIUS_TYPE
                ? WearUtils.ACTION_SET_GUIDED_ALTITUDE
                : WearUtils.ACTION_SET_FOLLOW_ME_RADIUS;
        final Integer radius = (Integer) viewHolder.itemView.getTag();
        activity.startService(new Intent(activity.getApplicationContext(), WearReceiverService.class)
                .setAction(action)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, radius));
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
