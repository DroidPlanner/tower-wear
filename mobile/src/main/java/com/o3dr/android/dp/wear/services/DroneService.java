package com.o3dr.android.dp.wear.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceManager;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.ServiceListener;
import com.o3dr.android.dp.wear.activities.PreferencesActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.GoogleApiClientManager;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.utils.AppPreferences;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.util.ParcelableUtils;

/**
 * Created by fhuya on 12/27/14.
 */
public class DroneService extends Service implements ServiceListener, DroneListener {

    private static final String TAG = DroneService.class.getSimpleName();

    private static final long WATCHDOG_TIMEOUT = 30 * 1000; //ms

    private final Handler handler = new Handler();

    private final Runnable destroyWatchdog = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);

            if(drone == null || !drone.isConnected()){
                stopSelf();
            }

            handler.postDelayed(this, WATCHDOG_TIMEOUT);
        }
    };

    private AppPreferences appPrefs;
    private ServiceManager serviceManager;
    private Drone drone;

    protected GoogleApiClientManager apiClientMgr;

    @Override
    public void onCreate(){
        super.onCreate();

        final Context context = getApplicationContext();
        appPrefs = new AppPreferences(context);
        apiClientMgr = new GoogleApiClientManager(context, handler, Wearable.API);
        apiClientMgr.start();

        serviceManager = new ServiceManager(context);
        serviceManager.connect(this);

        this.drone = new Drone(serviceManager, handler);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        apiClientMgr.stop();

        //Clean out the service manager, and drone instances.

        handler.removeCallbacks(destroyWatchdog);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            final String action = intent.getAction();
            if(action != null){

                switch(action){
                    case WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION:
                        final State vehicleState = drone.getAttribute(AttributeType.STATE);
                        byte[] stateData = ParcelableUtils.marshall(vehicleState);
                        sendMessage(action, stateData);
                        break;

                    case WearUtils.ACTION_CONNECT:
                        final ConnectionParameter connParams = retrieveConnectionParameters();
                        if(connParams != null)
                            drone.connect(connParams);
                        break;

                    case WearUtils.ACTION_DISCONNECT:
                        drone.disconnect();
                        break;
                }
            }
        }

        //Start a watchdog to automatically stop the service when it's no longer needed.
        handler.removeCallbacks(destroyWatchdog);
        handler.postDelayed(destroyWatchdog, WATCHDOG_TIMEOUT);

        return START_REDELIVER_INTENT;
    }

    private ConnectionParameter retrieveConnectionParameters(){
        final int connectionType = appPrefs.getConnectionParameterType();
        switch(connectionType){
            case ConnectionType.TYPE_UDP:
                return null;

            case ConnectionType.TYPE_USB:
                return null;

            case ConnectionType.TYPE_TCP:
                return null;

            case ConnectionType.TYPE_BLUETOOTH:
                return null;
        }
        return null;
    }

    protected boolean sendMessage(String msgPath, byte[] msgData){
        return WearUtils.asyncSendMessage(apiClientMgr, msgPath, msgData);
    }

    @Override
    public void onServiceConnected() {
        if(!drone.isStarted()) {
            drone.start();
            drone.registerDroneListener(this);
        }
    }

    @Override
    public void onServiceInterrupted() {

    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDroneEvent(String event, Bundle bundle) {
        sendMessage(WearUtils.EVENT_PREFIX + event, null);
    }

    @Override
    public void onDroneServiceInterrupted(String s) {
        drone.destroy();
    }
}
