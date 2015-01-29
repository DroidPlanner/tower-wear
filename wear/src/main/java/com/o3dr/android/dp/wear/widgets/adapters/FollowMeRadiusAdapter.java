package com.o3dr.android.dp.wear.widgets.adapters;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.unit.providers.length.LengthUnitProvider;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.length.LengthUnit;
import org.w3c.dom.Text;

/**
 * Created by fhuya on 1/4/15.
 */
public class FollowMeRadiusAdapter extends WearableListView.Adapter {

    public static final int MIN_RADIUS = 3; //meters
    private static final int MAX_RADIUS = 200; //meters

    protected static class ViewHolder extends WearableListView.ViewHolder {
        final TextView radiusView;

        public ViewHolder(View itemView){
            super(itemView);
            radiusView = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private LengthUnitProvider lengthUnitProvider;
    private LengthUnit minRadius;
    private LengthUnit maxRadius;

    public FollowMeRadiusAdapter(LengthUnitProvider lengthUnitProvider){
        setLengthUnitProvider(lengthUnitProvider);
    }

    public void setLengthUnitProvider(LengthUnitProvider lengthUnitProvider){
        this.lengthUnitProvider = lengthUnitProvider;

        LengthUnit convertedMin = lengthUnitProvider.boxBaseValueToTarget(MIN_RADIUS);
        minRadius = (LengthUnit) convertedMin.valueOf(Math.round(convertedMin.getValue()));

        LengthUnit convertedMax = lengthUnitProvider.boxBaseValueToTarget(MAX_RADIUS);
        maxRadius = (LengthUnit) convertedMax.valueOf(Math.round(convertedMax.getValue()));

        notifyDataSetChanged();
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wear_list_item_default, parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final LengthUnit radius = (LengthUnit) minRadius.valueOf(minRadius.getValue() + position);
        ((ViewHolder)holder).radiusView.setText(radius.toString());
        holder.itemView.setTag(radius);
    }

    @Override
    public int getItemCount() {
        return (int) (Operation.sub(maxRadius, minRadius).getValue()  + 1);
    }
}
