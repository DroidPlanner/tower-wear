package com.o3dr.android.dp.wear.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.gcs.FollowApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.android.dp.wear.fragments.SettingsFragment;
import com.o3dr.android.dp.wear.lib.utils.GoogleApiClientManager;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.utils.AppPreferences;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import java.util.LinkedList;

/**
 * Created by fhuya on 12/27/14.
 */
public class DroneService extends Service implements TowerListener, DroneListener {

    private static final String TAG = DroneService.class.getSimpleName();

    private static final long WATCHDOG_TIMEOUT = 30 * 1000; //ms

    static final String EXTRA_ACTION_DATA = "extra_action_data";
    public static final String EXTRA_CONNECTION_PARAMETER = "extra_connection_parameter";

    private final static IntentFilter intentFilter = new IntentFilter(SettingsFragment.ACTION_PREFERENCES_UPDATED);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case SettingsFragment.ACTION_PREFERENCES_UPDATED:
                    updateAppPreferences();
                    break;
            }
        }
    };

    private final Handler handler = new Handler();

    private final Runnable destroyWatchdog = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);

            if (drone == null || !drone.isConnected()) {
                stopSelf();
            }

            handler.postDelayed(this, WATCHDOG_TIMEOUT);
        }
    };

    private final LinkedList<Runnable> droneActionsQueue = new LinkedList<>();

    private AppPreferences appPrefs;
    private ControlTower controlTower;
    private Drone drone;

    protected GoogleApiClientManager apiClientMgr;

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();
        appPrefs = new AppPreferences(context);
        apiClientMgr = new GoogleApiClientManager(context, handler, Wearable.API);
        apiClientMgr.start();
        updateAppPreferences();

        controlTower = new ControlTower(context);
        controlTower.connect(this);

        this.drone = new Drone();

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);

        apiClientMgr.stop();

        //Clean out the service manager, and drone instances.
        controlTower.unregisterDrone(drone);
        controlTower.disconnect();

        handler.removeCallbacks(destroyWatchdog);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {

                switch (action) {
                    case WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION:
                        sendMessage(action, null);

                        if (!drone.isConnected()) {
                            //Check if the Tower app connected behind our back.
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    Bundle[] appsInfo = controlTower.getConnectedApps();
                                    if (appsInfo == null)
                                        return;

                                    for (Bundle info : appsInfo) {
                                        info.setClassLoader(ConnectionParameter.class.getClassLoader());
                                        final String appId = info.getString(GCSEvent.EXTRA_APP_ID);
                                        if (WearUtils.TOWER_APP_ID.equals(appId)) {
                                            final ConnectionParameter connParams = info.getParcelable(GCSEvent
                                                    .EXTRA_VEHICLE_CONNECTION_PARAMETER);
                                            if (connParams != null)
                                                executeDroneAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        drone.connect(connParams);
                                                    }
                                                });
                                            return;
                                        }
                                    }
                                }
                            });
                        }
                        break;

                    case WearUtils.ACTION_CONNECT:
                        final ConnectionParameter connParams = intent.getParcelableExtra(EXTRA_CONNECTION_PARAMETER);
                        if (connParams != null)
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    drone.connect(connParams);
                                }
                            });
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
                        if (vehicleMode != null) {
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
                                if (followState != null)
                                    followType = followState.getMode();

                                if (followType == null)
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
                        if (followType != null) {
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
                        if (altitude != -1) {
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
                        if (radius != -1) {
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    Bundle params = new Bundle();
                                    params.putDouble(FollowType.EXTRA_FOLLOW_RADIUS, radius);
                                    FollowApi.updateFollowParams(drone, params);
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

    private byte[] getDroneAttribute(String attributeType) {
        final Parcelable attribute = drone.getAttribute(attributeType);
        return attribute == null ? null : ParcelableUtils.marshall(attribute);
    }

    private void executeDroneAction(final Runnable action) {
        if (drone.isStarted())
            action.run();
        else {
            droneActionsQueue.offer(action);
        }
    }

    protected boolean sendMessage(String msgPath, byte[] msgData) {
        return WearUtils.asyncSendMessage(apiClientMgr, msgPath, msgData);
    }

    @Override
    public void onTowerConnected() {
        Log.d(TAG, "3DR Services connected.");
        if (!drone.isStarted()) {
            drone.registerDroneListener(this);
            controlTower.registerDrone(drone, handler);
            updateAllVehicleAttributes();
            Log.d(TAG, "Drone started.");

            if (!droneActionsQueue.isEmpty()) {
                for (Runnable action : droneActionsQueue) {
                    action.run();
                }
            }
        }
    }

    @Override
    public void onTowerDisconnected() {

    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDroneEvent(String event, Bundle bundle) {
        String attributeType = null;
        switch (event) {
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

            case AttributeEvent.GPS_POSITION:
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

            case AttributeEvent.HOME_UPDATED:
                attributeType = AttributeType.HOME;
                break;
        }

        updateVehicleAttribute(attributeType);
    }

    private void updateAllVehicleAttributes() {
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

    private void updateVehicleAttribute(String attributeType) {
        if (attributeType != null) {
            byte[] eventData = getDroneAttribute(attributeType);
            String dataPath = WearUtils.VEHICLE_DATA_PREFIX + attributeType;
            WearUtils.asyncPutDataItem(apiClientMgr, dataPath, eventData);
        }
    }

    private void updateAppPreferences(){
        //Updating hdop preference
        byte[] hdopEnabled = {(byte) (appPrefs.isGpsHdopEnabled() ? 1 : 0)};
        WearUtils.asyncPutDataItem(apiClientMgr, WearUtils.PREF_IS_HDOP_ENABLED, hdopEnabled);

        //Updating permanent notification preference
        byte[] isNotificationPermanent = {(byte)(appPrefs.isNotificationPermanent()? 1 : 0)};
        WearUtils.asyncPutDataItem(apiClientMgr, WearUtils.PREF_NOTIFICATION_PERMANENT, isNotificationPermanent);

        //Updating screen stays on preference
        byte[] screenStaysOn = {(byte) (appPrefs.keepScreenBright() ? 1 : 0)};
        WearUtils.asyncPutDataItem(apiClientMgr, WearUtils.PREF_SCREEN_STAYS_ON, screenStaysOn);

        //Updating unit system preference
        byte[] unitSystem = {(byte) appPrefs.getUnitSystemType()};
        WearUtils.asyncPutDataItem(apiClientMgr, WearUtils.PREF_UNIT_SYSTEM, unitSystem);
    }

    @Override
    public void onDroneServiceInterrupted(String s) {
        controlTower.unregisterDrone(drone);
    }
}
