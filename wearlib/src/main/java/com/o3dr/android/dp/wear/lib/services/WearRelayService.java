package com.o3dr.android.dp.wear.lib.services;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.o3dr.android.dp.wear.lib.utils.GoogleApiClientManager;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearRelayService extends WearableListenerService {

    private final static String TAG = WearRelayService.class.getSimpleName();

    protected final Handler handler = new Handler();
    protected GoogleApiClientManager apiClientMgr;

    @Override
    public void onCreate(){
        super.onCreate();
        apiClientMgr = new GoogleApiClientManager(getApplicationContext(), handler, Wearable.API);
        apiClientMgr.start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        apiClientMgr.stop();
    }

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

    protected boolean sendMessage(String msgPath, byte[] msgData){
        return WearUtils.asyncSendMessage(apiClientMgr, msgPath, msgData);
    }

    protected void onEventReceived(String attributeEvent, byte[] data){}

    protected void onActionRequested(String actionPath, byte[] data){}
}
