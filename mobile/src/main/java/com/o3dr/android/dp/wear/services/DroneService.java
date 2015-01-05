package com.o3dr.android.dp.wear.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceManager;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.ServiceListener;
import com.o3dr.android.dp.wear.activities.BluetoothDevicesActivity;
import com.o3dr.android.dp.wear.lib.utils.GoogleApiClientManager;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.utils.AppPreferences;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.connection.StreamRates;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import java.util.LinkedList;

/**
 * Created by fhuya on 12/27/14.
 */
public class DroneService extends Service implements ServiceListener, DroneListener {

    private static final String TAG = DroneService.class.getSimpleName();

    private static final long WATCHDOG_TIMEOUT = 30 * 1000; //ms

    static final String EXTRA_ACTION_DATA = "extra_action_data";

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

    private final LinkedList<Runnable> droneActionsQueue = new LinkedList<>();

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
        drone.destroy();
        serviceManager.disconnect();

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
                        sendMessage(action, null);
                        break;

                    case WearUtils.ACTION_CONNECT:
                        final ConnectionParameter connParams = retrieveConnectionParameters();
                        if(connParams != null) {
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    drone.connect(connParams);
                                }
                            });
                        }
                        break;

                    case WearUtils.ACTION_DISCONNECT:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                drone.disconnect();
                            }
                        });
                        break;

                    case WearUtils.ACTION_ARM:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                drone.arm(true);
                            }
                        });
                        break;

                    case WearUtils.ACTION_DISARM:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                drone.arm(false);
                            }
                        });
                        break;

                    case WearUtils.ACTION_TAKE_OFF:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                drone.doGuidedTakeoff(5);
                            }
                        });
                        break;

                    case WearUtils.ACTION_CHANGE_VEHICLE_MODE:
                        final VehicleMode vehicleMode = intent.getParcelableExtra(EXTRA_ACTION_DATA);
                        if(vehicleMode != null){
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    drone.changeVehicleMode(vehicleMode);
                                }
                            });
                        }
                        break;

                    case WearUtils.ACTION_START_FOLLOW_ME:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
                                FollowType followType = null;
                                if(followState != null)
                                    followType = followState.getMode();

                                if(followType == null)
                                    followType = FollowType.LEASH;
                                drone.enableFollowMe(followType);
                            }
                        });
                        break;

                    case WearUtils.ACTION_STOP_FOLLOW_ME:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                drone.disableFollowMe();
                            }
                        });
                        break;

                    case WearUtils.ACTION_CHANGE_FOLLOW_ME_TYPE:
                        final FollowType followType = intent.getParcelableExtra(EXTRA_ACTION_DATA);
                        if(followType != null) {
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    drone.enableFollowMe(followType);
                                }
                            });
                        }
                        break;

                    case WearUtils.ACTION_SET_GUIDED_ALTITUDE:
                        final int altitude = intent.getIntExtra(EXTRA_ACTION_DATA, -1);
                        if(altitude != -1){
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    drone.setGuidedAltitude(altitude);
                                }
                            });
                        }
                        break;

                    case WearUtils.ACTION_SET_FOLLOW_ME_RADIUS:
                        final int radius = intent.getIntExtra(EXTRA_ACTION_DATA, -1);
                        if(radius != -1){
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    drone.setFollowMeRadius(radius);
                                }
                            });
                        }
                        break;
                }
            }
        }

        //Start a watchdog to automatically stop the service when it's no longer needed.
        handler.removeCallbacks(destroyWatchdog);
        handler.postDelayed(destroyWatchdog, WATCHDOG_TIMEOUT);

        return START_REDELIVER_INTENT;
    }

    private byte[] getDroneAttribute(String attributeType){
        final Parcelable attribute = drone.getAttribute(attributeType);
        return attribute == null ? null : ParcelableUtils.marshall(attribute);
    }

    private void executeDroneAction(final Runnable action){
        if(drone.isStarted())
            action.run();
        else{
            droneActionsQueue.offer(action);
        }
    }

    private ConnectionParameter retrieveConnectionParameters(){
        final int connectionType = appPrefs.getConnectionParameterType();
        final StreamRates rates = appPrefs.getStreamRates();
        Bundle extraParams = new Bundle();
        final DroneSharePrefs droneSharePrefs = new DroneSharePrefs(appPrefs.getDroneshareLogin(),
                appPrefs.getDronesharePassword(), appPrefs.getDroneshareEnabled(), appPrefs.getLiveUploadEnabled());

        ConnectionParameter connParams;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, appPrefs.getUsbBaudRate());
                connParams = new ConnectionParameter(connectionType, extraParams, rates,
                        droneSharePrefs);
                break;

            case ConnectionType.TYPE_UDP:
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, appPrefs.getUdpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams, rates,
                        droneSharePrefs);
                break;

            case ConnectionType.TYPE_TCP:
                extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, appPrefs.getTcpServerIp());
                extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, appPrefs.getTcpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams, rates,
                        droneSharePrefs);
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                String btAddress = appPrefs.getBluetoothDeviceAddress();
                if (TextUtils.isEmpty(btAddress)) {
                    connParams = null;
                    startActivity(new Intent(getApplicationContext(), BluetoothDevicesActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                } else {
                    extraParams.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, btAddress);
                    connParams = new ConnectionParameter(connectionType, extraParams, rates,
                            droneSharePrefs);
                }
                break;

            default:
                Log.e(TAG, "Unrecognized connection type: " + connectionType);
                connParams = null;
                break;
        }

        return connParams;
    }

    protected boolean sendMessage(String msgPath, byte[] msgData){
        return WearUtils.asyncSendMessage(apiClientMgr, msgPath, msgData);
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "3DR Services connected.");
        if(!drone.isStarted()) {
            drone.registerDroneListener(this);
            drone.start();
            updateAllVehicleAttributes();
            Log.d(TAG, "Drone started.");

            if(!droneActionsQueue.isEmpty()){
                for(Runnable action: droneActionsQueue){
                    action.run();
                }
            }
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
        String attributeType = null;
        switch(event){
            case AttributeEvent.STATE_CONNECTED:
            case AttributeEvent.STATE_DISCONNECTED:
                //Update all of the vehicle's properties.
                updateAllVehicleAttributes();
                break;

            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_VEHICLE_MODE:
            case AttributeEvent.STATE_ARMING:
                //Retrieve the state attribute
                attributeType = AttributeType.STATE;
                break;

            case AttributeEvent.BATTERY_UPDATED:
                //Retrieve the battery attribute
                attributeType = AttributeType.BATTERY;
                break;

            case AttributeEvent.SIGNAL_UPDATED:
                //Retrieve the signal attribute
                attributeType = AttributeType.SIGNAL;
                break;

            case AttributeEvent.GPS_FIX:
            case AttributeEvent.GPS_COUNT:
                attributeType = AttributeType.GPS;
                break;

            case AttributeEvent.GUIDED_POINT_UPDATED:
                attributeType = AttributeType.GUIDED_STATE;
                break;

            case AttributeEvent.FOLLOW_START:
            case AttributeEvent.FOLLOW_STOP:
            case AttributeEvent.FOLLOW_UPDATE:
                attributeType = AttributeType.FOLLOW_STATE;
                break;
        }

        updateVehicleAttribute(attributeType);
    }

    private void updateAllVehicleAttributes(){
        updateVehicleAttribute(AttributeType.ALTITUDE);
        updateVehicleAttribute(AttributeType.ATTITUDE);
        updateVehicleAttribute(AttributeType.BATTERY);
        updateVehicleAttribute(AttributeType.FOLLOW_STATE);
        updateVehicleAttribute(AttributeType.GUIDED_STATE);
        updateVehicleAttribute(AttributeType.GPS);
        updateVehicleAttribute(AttributeType.HOME);
        updateVehicleAttribute(AttributeType.STATE);
        updateVehicleAttribute(AttributeType.TYPE);
    }

    private void updateVehicleAttribute(String attributeType){
        if(attributeType != null) {
            byte[] eventData = getDroneAttribute(attributeType);
            String dataPath = WearUtils.VEHICLE_DATA_PREFIX + attributeType;
            WearUtils.asyncPutDataItem(apiClientMgr, dataPath, eventData);
        }
    }

    @Override
    public void onDroneServiceInterrupted(String s) {
        drone.destroy();
    }
}
