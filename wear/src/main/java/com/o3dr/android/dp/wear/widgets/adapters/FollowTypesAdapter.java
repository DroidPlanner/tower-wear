package com.o3dr.android.dp.wear.widgets.adapters;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

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

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wear_list_item_default,
                parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final FollowType followType = FollowType.values()[position];
        ((ViewHolder)holder).followTypeView.setText(followType.getTypeLabel());
        holder.itemView.setTag(followType);
    }

    @Override
    public int getItemCount() {
        return FollowType.values().length;
    }
}
