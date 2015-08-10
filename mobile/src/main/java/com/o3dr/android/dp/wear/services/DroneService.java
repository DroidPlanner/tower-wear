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
import android.widget.Toast;

import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_set_position_target_local_ned;
import com.MAVLink.enums.MAV_CMD;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.android.dp.wear.activities.InstallAndroidWearApp;
import com.o3dr.android.dp.wear.activities.ResolveGooglePlayServicesIssue;
import com.o3dr.android.dp.wear.lib.utils.AppPreferences;
import com.o3dr.android.dp.wear.lib.utils.WearFollowState;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.util.ParcelableUtils;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;

import java.util.LinkedList;

import timber.log.Timber;

/**
 * Created by fhuya on 12/27/14.
 */
public class DroneService extends Service implements TowerListener, DroneListener, GoogleApiClientManager.ManagerListener {

    private static final String TAG = DroneService.class.getSimpleName();

    private static final long WATCHDOG_TIMEOUT = 5 * 1000; //ms

    static final String EXTRA_ACTION_DATA = "extra_action_data";
    public static final String EXTRA_CONNECTION_PARAMETER = "extra_connection_parameter";
    public static final String EXTRA_ERROR_CODE = "extra_error_code";

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{Wearable.API};

    private static final int ignoreVel = ((1<<3) | (1<<4) | (1 << 5));
    private static final int ignoreAcc = ((1<<6) | (1<<7) | (1 << 8));
    private static final int ignorePos = ((1<<0) | (1<<1) | (1<<2));

    private final static IntentFilter intentFilter = new IntentFilter(WearUtils.ACTION_PREFERENCES_UPDATED);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WearUtils.ACTION_PREFERENCES_UPDATED:
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

    private LocalBroadcastManager lbm;
    private AppPreferences appPrefs;
    private ControlTower controlTower;
    private Drone drone;

    protected GoogleApiClientManager apiClientMgr;

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();
        lbm = LocalBroadcastManager.getInstance(context);
        appPrefs = new AppPreferences(context);
        apiClientMgr = new GoogleApiClientManager(context, handler, apisList);
        apiClientMgr.setManagerListener(this);
        apiClientMgr.start();

        updateAppPreferences();

        controlTower = new ControlTower(context);
        controlTower.connect(this);

        this.drone = new Drone(context);

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);

        apiClientMgr.stopSafely();

        //Clean out the service manager, and drone instances.
        Log.d(TAG, "Disconnecting from the control tower.");
        Toast.makeText(getApplicationContext(), "Disconnecting from vehicle", Toast.LENGTH_LONG).show();
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

                    case WearUtils.ACTION_STREAM_NOTIFICATION_SHOWN:
                        lbm.sendBroadcast(new Intent(WearUtils.ACTION_STREAM_NOTIFICATION_SHOWN));
                        break;

                    case WearUtils.ACTION_CONNECT:
                        final ConnectionParameter connParams = intent.getParcelableExtra(EXTRA_CONNECTION_PARAMETER);
                        if (connParams != null)
                            executeDroneAction(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "Connecting to the vehicle.");
                                    drone.connect(connParams);
                                }
                            });
                        break;

                    case WearUtils.ACTION_DISCONNECT:
                        executeDroneAction(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Disconnecting from the vehicle.");
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
                                drone.doGuidedTakeoff(10);
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
                                    FollowApi.getApi(drone).updateFollowParams(params);
                                }
                            });
                        }
                        break;
                    case WearUtils.ACTION_DRIFT_CONTROL:{
                        final float[] stickValue = intent.getFloatArrayExtra(EXTRA_ACTION_DATA);
                        final float yaw = stickValue[0];
                        float y = stickValue[1];
                        float x = 0f;
                        Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
                        float heading = (float) attitude.getYaw();
//                        heading /= Math.PI;
//                        heading *= 180f;
                        Log.d("yaw control", "yaw: " + yaw + ", heading:" + heading);
                        if (Math.abs(yaw) > 0.05) {
                            msg_command_long msgYaw = new msg_command_long();
                            msgYaw.command = MAV_CMD.MAV_CMD_CONDITION_YAW;
                            msgYaw.param1 = (360 + (heading + yaw * 30f)) % 360;
                            msgYaw.param2 = Math.abs(yaw) * 30f;
                            msgYaw.param3 = Math.signum(yaw);
                            msgYaw.param4 = 0;
                            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(msgYaw));
                        }
                        if (y != 0) {
                            double theta = 0;
                            if (theta < 0) {
                                theta += Math.PI;
                            }
                            if (y < 0) {
                                theta += Math.PI;
                            }
//                            theta += Math.PI / 2;
                            double magnitude = y;
                            heading = (float)Math.toRadians(heading);
                            x = (float) (Math.cos(heading + theta) * magnitude);
                            y = (float) (Math.sin(heading + theta) * magnitude);
                        }
                        Log.d("drift control", x + ", " + y);
                        msg_set_position_target_local_ned msg = new msg_set_position_target_local_ned();
                        msg.vy = y* 5f;
                        msg.vx = x * 5f;
                        msg.type_mask = ignoreAcc | ignorePos;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(msg));
                        break;
                }
                    case WearUtils.ACTION_DRIFT_STOP: {
                        msg_set_position_target_local_ned msg = new msg_set_position_target_local_ned();
                        msg.vy = 0;
                        msg.vx = 0;
                        msg.vz = 0;
                        msg.type_mask = ignoreAcc | ignorePos;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(msg));
                        break;
                    }

                }
            }
        }

        //Start a watchdog to automatically stop the service when it's no longer needed.
        handler.removeCallbacks(destroyWatchdog);
        handler.postDelayed(destroyWatchdog, WATCHDOG_TIMEOUT);

        return START_REDELIVER_INTENT;
    }

    private byte[] getDroneAttribute(String attributeType) {
        Parcelable attribute = drone.getAttribute(attributeType);
        if (attribute instanceof FollowState) {
            attribute = new WearFollowState((FollowState) attribute);
        }

        return attribute == null ? null : ParcelableUtils.marshall(attribute);
    }

    private void executeDroneAction(final Runnable action) {
        if (drone.isStarted()) {
            Log.d(TAG, "Running drone action.");
            action.run();
        } else {
            Log.d(TAG, "Queuing drone action.");
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
            Toast.makeText(getApplicationContext(), "Drone started: " + drone.isConnected(), Toast.LENGTH_LONG).show();

            if (!droneActionsQueue.isEmpty()) {
                for (Runnable action : droneActionsQueue) {
                    action.run();
                }
            }
        }
    }

    @Override
    public void onTowerDisconnected() {
//        controlTower.unregisterDrone(drone);
//        controlTower.disconnect();
        stopSelf();
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
                Log.d(TAG, "Received drone connection event: " + event);
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

    private void updateAppPreferences() {
        //Updating hdop preference
        byte[] hdopEnabled = {(byte) (appPrefs.isGpsHdopEnabled() ? 1 : 0)};
        WearUtils.asyncPutDataItem(apiClientMgr, WearUtils.PREF_IS_HDOP_ENABLED, hdopEnabled);

        //Updating permanent notification preference
        byte[] isNotificationPermanent = {(byte) (appPrefs.isNotificationPermanent() ? 1 : 0)};
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

    @Override
    public void onGoogleApiConnectionError(com.google.android.gms.common.ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == com.google.android.gms.common.ConnectionResult.API_UNAVAILABLE) {
            //The android wear app is not installed.
            startActivity(new Intent(getApplicationContext(), InstallAndroidWearApp.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            stopSelf();
        }
    }

    @Override
    public void onUnavailableGooglePlayServices(int i) {
        startActivity(new Intent(getApplicationContext(), ResolveGooglePlayServicesIssue.class)
                .putExtra(EXTRA_ERROR_CODE, i)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        stopSelf();
    }

    @Override
    public void onManagerStarted() {

    }

    @Override
    public void onManagerStopped() {

    }
}
