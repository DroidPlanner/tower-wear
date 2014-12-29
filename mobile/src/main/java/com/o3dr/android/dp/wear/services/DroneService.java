package com.o3dr.android.dp.wear.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceManager;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;

/**
 * Created by fhuya on 12/27/14.
 */
public class DroneService extends WearRelayService {

    private static final String TAG = DroneService.class.getSimpleName();

    private ServiceManager serviceManager;
    private Drone drone;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Send a message to the wear app
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onActionRequested(String actionPath, byte[] data){
        switch(actionPath){
            case WearUtils.ACTION_CONNECT:
                break;

            case WearUtils.ACTION_DISCONNECT:
                break;

            case WearUtils.ACTION_REQUEST_ATTRIBUTE:
                //Retrieve the attribute to request from the data argument.

                break;
        }
    }

    @Override
    public void onPeerConnected(Node peer){
        Log.d(TAG, "Connected to wear node " + peer.getDisplayName());
    }
}
