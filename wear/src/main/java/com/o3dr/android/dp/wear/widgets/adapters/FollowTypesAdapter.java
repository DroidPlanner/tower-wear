package com.o3dr.android.dp.wear.widgets.adapters;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import java.util.List;

/**
* Created by fhuya on 1/3/15.
*/
public class FollowTypesAdapter extends WearableListView.Adapter {

    protected static class ViewHolder extends WearableListView.ViewHolder {

        final TextView followTypeView;

        public ViewHolder(View itemView){
            super(itemView);
            followTypeView = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private final List<FollowType> followTypes = FollowType.getFollowTypes(false);

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wear_list_item_default, parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final FollowType followType = followTypes.get(position);
        ((ViewHolder)holder).followTypeView.setText(followType.getTypeLabel());
        holder.itemView.setTag(followType);
    }

    @Override
    public int getItemCount() {
        return followTypes.size();
    }
}
