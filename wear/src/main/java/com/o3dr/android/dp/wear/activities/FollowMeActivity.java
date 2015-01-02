package com.o3dr.android.dp.wear.activities;

import android.content.Intent;
import android.os.Bundle;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;

/**
 * Created by fhuya on 12/28/14.
 */
public class FollowMeActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_control);

        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION));
    }

    @Override
    protected void onVehicleDataUpdated(String dataType, byte[] eventData) {
        switch(dataType){
            case AttributeType.FOLLOW_STATE:
                break;
        }
    }
}
