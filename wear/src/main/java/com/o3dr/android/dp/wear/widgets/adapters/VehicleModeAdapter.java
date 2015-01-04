package com.o3dr.android.dp.wear.widgets.adapters;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import java.util.List;

/**
* Created by fhuya on 1/1/15.
*/
public class VehicleModeAdapter extends WearableListView.Adapter {

    protected static class ViewHolder extends WearableListView.ViewHolder {

        final TextView vehicleModeNameView;

        public ViewHolder(View itemView) {
            super(itemView);
            vehicleModeNameView = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private final List<VehicleMode> vehicleModes;

    public VehicleModeAdapter(List<VehicleMode> vehicleModes){
        this.vehicleModes = vehicleModes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .wear_list_item_default, parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position){
        final VehicleMode vehicleMode = vehicleModes.get(position);
        ((ViewHolder)holder).vehicleModeNameView.setText(vehicleMode.getLabel());
        holder.itemView.setTag(vehicleMode);
    }

    @Override
    public int getItemCount(){
        return vehicleModes.size();
    }
}
