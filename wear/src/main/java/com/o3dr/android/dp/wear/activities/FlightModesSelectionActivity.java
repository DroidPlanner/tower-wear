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
import com.o3dr.android.dp.wear.widgets.adapters.VehicleModeAdapter;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import java.util.List;

/**
 * Created by fhuya on 12/29/14.
 */
public class FlightModesSelectionActivity extends Activity implements WearableListView.ClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_modes_selector);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        VehicleMode flightMode = intent.getParcelableExtra(WearReceiverService.EXTRA_EVENT_DATA);
        if(flightMode == null)
            flightMode = VehicleMode.UNKNOWN;

        List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(flightMode.getDroneType());
        WearableListView listView = (WearableListView) findViewById(R.id.flight_modes_list);
        listView.setAdapter(new VehicleModeAdapter(vehicleModes));
        listView.setClickListener(this);

        RecyclerView.LayoutManager layoutMgr = listView.getLayoutManager();
        if (layoutMgr != null) {
            int currentPos = vehicleModes.indexOf(flightMode);
            if (currentPos != -1)
                layoutMgr.scrollToPosition(currentPos);
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final VehicleMode selected = (VehicleMode) viewHolder.itemView.getTag();
        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_CHANGE_VEHICLE_MODE)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, (Parcelable) selected));

        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

}
