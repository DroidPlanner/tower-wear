package com.o3dr.android.dp.wear.services;

import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearReceiverService extends WearRelayService {

    @Override
    protected void onEventReceived(String attributeEvent, byte[] data){
        switch(attributeEvent){
            case AttributeEvent.STATE_CONNECTED:
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                break;
        }
    }
}
