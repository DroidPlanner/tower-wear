package com.o3dr.android.dp.wear.widgets.adapters;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;

import org.w3c.dom.Text;

/**
 * Created by fhuya on 1/4/15.
 */
public class FollowMeRadiusAdapter extends WearableListView.Adapter {

    public static final int MIN_RADIUS = 2; //meters
    private static final int MAX_RADIUS = 200; //meters

    protected static class ViewHolder extends WearableListView.ViewHolder {
        final TextView radiusView;

        public ViewHolder(View itemView){
            super(itemView);
            radiusView = (TextView) itemView.findViewById(R.id.name);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wear_list_item_default,
                parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final Integer radius = position + MIN_RADIUS;
        ((ViewHolder)holder).radiusView.setText(radius.toString());
        holder.itemView.setTag(radius);
    }

    @Override
    public int getItemCount() {
        return MAX_RADIUS - MIN_RADIUS + 1;
    }
}
