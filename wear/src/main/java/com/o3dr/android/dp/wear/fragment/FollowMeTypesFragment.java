package com.o3dr.android.dp.wear.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.FollowMeActivity;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.adapters.FollowTypesAdapter;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

/**
 * Created by fhuya on 1/3/15.
 */
public class FollowMeTypesFragment extends Fragment implements WearableListView.ClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wear_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        final FollowState followState = arguments.getParcelable(FollowMeActivity.EXTRA_VEHICLE_FOLLOW_STATE);

        WearableListView followTypesView = (WearableListView) view.findViewById(R.id.wear_list);
        followTypesView.setAdapter(new FollowTypesAdapter());
        followTypesView.setClickListener(this);

        RecyclerView.LayoutManager layoutMgr = followTypesView.getLayoutManager();
        if (layoutMgr != null) {
            layoutMgr.scrollToPosition(followState.getMode().ordinal());
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        final FollowType followType = (FollowType) viewHolder.itemView.getTag();
        activity.startService(new Intent(activity.getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_CHANGE_FOLLOW_ME_TYPE)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, (Parcelable) followType));
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
