package com.o3dr.android.dp.wear.lib.services;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearRelayService extends WearableListenerService {

    private final static String TAG = WearRelayService.class.getSimpleName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        final String msgPath = messageEvent.getPath();
        if(msgPath.startsWith(WearUtils.EVENT_PREFIX)){
            final String attributeEvent = msgPath.replace(WearUtils.EVENT_PREFIX, "");
            onEventReceived(attributeEvent, messageEvent.getData());
        }
        else if(msgPath.startsWith(WearUtils.ACTION_PREFIX)){
            onActionRequested(msgPath, messageEvent.getData());
        }
        else{
            Log.w(TAG, "Unrecognized message event path: " + msgPath);
        }
    }

    protected void onEventReceived(String attributeEvent, byte[] data){}

    protected void onActionRequested(String actionPath, byte[] data){}
}
