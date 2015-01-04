package com.o3dr.android.dp.wear.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.ContextStreamActivity;
import com.o3dr.android.dp.wear.activities.FlightModesSelectionActivity;
import com.o3dr.android.dp.wear.activities.FollowMeActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.widgets.adapters.FollowMeRadiusAdapter;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearReceiverService extends WearRelayService {

    private final static String TAG = WearReceiverService.class.getSimpleName();

    private static final int WEAR_NOTIFICATION_ID = 101;

    public static final String EXTRA_EVENT_DATA = "extra_event_data";
    public static final String EXTRA_ACTION_DATA = "extra_action_data";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION:
                        updateContextStreamNotification();
                        break;

                    case WearUtils.ACTION_OPEN_PHONE_APP:
                        launchAnimation(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);

                        /* FALL - THROUGH */
                    case WearUtils.ACTION_ARM:
                    case WearUtils.ACTION_DISARM:
                    case WearUtils.ACTION_CONNECT:
                    case WearUtils.ACTION_DISCONNECT:
                    case WearUtils.ACTION_TAKE_OFF:
                    case WearUtils.ACTION_START_FOLLOW_ME:
                    case WearUtils.ACTION_STOP_FOLLOW_ME:
                        sendMessage(action, null);
                        break;

                    case WearUtils.ACTION_CHANGE_VEHICLE_MODE:
                        VehicleMode vehicleMode = intent.getParcelableExtra(EXTRA_ACTION_DATA);
                        byte[] actionData = vehicleMode == null ? null : ParcelableUtils.marshall(vehicleMode);
                        sendMessage(action, actionData);
                        break;

                    case WearUtils.ACTION_CHANGE_FOLLOW_ME_TYPE:
                        FollowType followType = intent.getParcelableExtra(EXTRA_ACTION_DATA);
                        byte[] followData = followType == null ? null : ParcelableUtils.marshall(followType);
                        sendMessage(action, followData);
                        break;

                    case WearUtils.ACTION_SET_FOLLOW_ME_RADIUS:
                    case WearUtils.ACTION_SET_GUIDED_ALTITUDE:
                        final int radius = intent.getIntExtra(EXTRA_ACTION_DATA, FollowMeRadiusAdapter.MIN_RADIUS);
                        byte[] radiusData = {(byte) radius};
                        sendMessage(action, radiusData);
                        break;
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void launchAnimation(int animationType) {
        startActivity(new Intent(getApplicationContext(), ConfirmationActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType));
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            final DataItem dataItem = event.getDataItem();
            final int eventType = event.getType();
            handleDataItem(dataItem, eventType);
        }
    }

    private void handleDataItem(DataItem dataItem, int eventType){
        final Uri dataUri = dataItem.getUri();
        final String dataPath = dataUri.getPath();

        if (dataPath.startsWith(WearUtils.VEHICLE_DATA_PREFIX)) {
            final String attributeType = dataPath.replace(WearUtils.VEHICLE_DATA_PREFIX, "");
            switch (attributeType) {
                case AttributeType.STATE:
                    if (eventType == DataEvent.TYPE_DELETED)
                        updateContextStreamNotification(null);
                    else {
                        byte[] eventData = dataItem.getData();
                        State vehicleState = eventData == null ? null : ParcelableUtils.unmarshall(eventData,
                                State.CREATOR);
                        updateContextStreamNotification(vehicleState);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActionRequested(String actionPath, byte[] actionData) {
        switch (actionPath) {
            case WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION:
                updateContextStreamNotification();
                break;
        }
    }

    private NotificationCompat.Action getDisconnectAction(Context context) {
        final Intent disconnectIntent = new Intent(context, WearReceiverService.class)
                .setAction(WearUtils.ACTION_DISCONNECT);
        final PendingIntent disconnectPI = PendingIntent.getService(context, 0, disconnectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action disconnectAction = new NotificationCompat.Action(R.drawable
                .ic_settings_power_white_48dp, getText(R.string.disconnect), disconnectPI);

        return disconnectAction;
    }

    private NotificationCompat.Action getFlightModesAction(Context context, VehicleMode vehicleMode) {
        if (vehicleMode == null)
            vehicleMode = VehicleMode.UNKNOWN;

        final Intent flightModesIntent = new Intent(context, FlightModesSelectionActivity.class)
                .setAction(AttributeEvent.STATE_VEHICLE_MODE)
                .putExtra(EXTRA_EVENT_DATA, (Parcelable) vehicleMode);
        final PendingIntent flightModesPI = PendingIntent.getActivity(context, 0, flightModesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action flightModeAction = new NotificationCompat.Action(R.drawable
                .ic_flight_white_48dp, vehicleMode.getLabel(), flightModesPI);

        return flightModeAction;
    }

    private NotificationCompat.Action getFollowMeAction(Context context, State vehicleState){
        final Intent openWearAppIntent = new Intent(context, FollowMeActivity.class)
                .setAction(AttributeType.STATE)
                .putExtra(EXTRA_EVENT_DATA, vehicleState);
        final PendingIntent openFollowMePI = PendingIntent.getActivity(context, 0, openWearAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action openFollowMeAction = new NotificationCompat.Action(R.drawable.ic_follow,
                getText(R.string.mission_control_follow), openFollowMePI);

        return openFollowMeAction;
    }

    private void updateContextStreamNotification(){
        final String dataPath = WearUtils.VEHICLE_DATA_PREFIX + AttributeType.STATE;
        apiClientMgr.addTask(apiClientMgr.new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final Uri dataItemUri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(dataPath).build();

                Wearable.DataApi.getDataItems(getGoogleApiClient(), dataItemUri)
                        .setResultCallback(new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {
                                final int dataCount = dataItems.getCount();
                                for(int i = 0; i < dataCount; i++){
                                    final DataItem dataItem = dataItems.get(i);
                                    handleDataItem(dataItem, DataEvent.TYPE_CHANGED);
                                }
                            }
                        });
            }
        });
    }

    private void updateContextStreamNotification(State vehicleState) {
        final Context context = getApplicationContext();
        final Resources res = getResources();

        final byte[] eventData = vehicleState == null ? null : ParcelableUtils.marshall(vehicleState);

        //Head card display
        final Intent displayIntent = new Intent(context, ContextStreamActivity.class)
                .setAction(AttributeType.STATE)
                .putExtra(EXTRA_EVENT_DATA, eventData);
        final PendingIntent displayPI = PendingIntent.getActivity(context, 0, displayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(res, R.drawable.wear_notification_bg))
                .setDisplayIntent(displayPI)
                .setCustomSizePreset(Notification.WearableExtender.SIZE_DEFAULT);

        final List<NotificationCompat.Action> actionsList = new ArrayList<>();

        //Flight action cards
        final boolean isConnected = vehicleState != null && vehicleState.isConnected();
        int notificationPriority = NotificationCompat.PRIORITY_DEFAULT;
        if (isConnected) {
            notificationPriority = NotificationCompat.PRIORITY_MAX;

            VehicleMode vehicleMode = vehicleState.getVehicleMode();
            final boolean isCopter = vehicleMode == null || vehicleMode.getDroneType() == Type.TYPE_COPTER;

            if (isCopter) {
                if (vehicleState.isFlying()) {
                    actionsList.add(getFollowMeAction(context, vehicleState));
                    actionsList.add(getFlightModesAction(context, vehicleMode));
                } else if (vehicleState.isArmed()) {
                    final Intent takeOffIntent = new Intent(context, WearReceiverService.class).setAction(WearUtils
                            .ACTION_TAKE_OFF);
                    final PendingIntent takeOffPI = PendingIntent.getService(context, 0, takeOffIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    final NotificationCompat.Action takeOffAction = new NotificationCompat.Action(R.drawable.ic_takeoff,
                            getText(R.string.take_off), takeOffPI);

                    actionsList.add(takeOffAction);

                    final Intent disarmIntent = new Intent(context, WearReceiverService.class).setAction(WearUtils
                            .ACTION_DISARM);
                    final PendingIntent disarmPI = PendingIntent.getService(context, 0, disarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    final NotificationCompat.Action disarmAction = new NotificationCompat.Action(R.drawable.ic_arm,
                            getText(R.string.disarm), disarmPI);

                    actionsList.add(disarmAction);
                } else {
                    final Intent armIntent = new Intent(context, WearReceiverService.class).setAction(WearUtils.ACTION_ARM);
                    final PendingIntent armPI = PendingIntent.getService(context, 0, armIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    final NotificationCompat.Action armAction = new NotificationCompat.Action(R.drawable.ic_arm,
                            getText(R.string.arm), armPI);

                    actionsList.add(armAction);

                    actionsList.add(getDisconnectAction(context));
                }
            } else {
                actionsList.add(getFollowMeAction(context, vehicleState));
                actionsList.add(getFlightModesAction(context, vehicleMode));
                actionsList.add(getDisconnectAction(context));
            }
        } else {
            final Intent connectIntent = new Intent(context, WearReceiverService.class)
                    .setAction(WearUtils.ACTION_CONNECT);
            final PendingIntent connectPI = PendingIntent.getService(context, 0, connectIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Action connectAction = new NotificationCompat
                    .Action(R.drawable.ic_settings_power_white_48dp, getText(R.string.connect), connectPI);

            actionsList.add(connectAction);
        }

        //Open phone app card.
        final Intent settingsIntent = new Intent(context, WearReceiverService.class)
                .setAction(WearUtils.ACTION_OPEN_PHONE_APP);
        final PendingIntent settingsPI = PendingIntent.getService(context, 0, settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action settingsAction = new NotificationCompat.Action(R.drawable.go_to_phone_00156,
                getText(R.string.preferences), settingsPI);

        actionsList.add(settingsAction);

        extender.addActions(actionsList);

        Notification contextStream = new NotificationCompat.Builder(context)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("")
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(notificationPriority)
                .setOngoing(isConnected)
                .extend(extender)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        Log.d(TAG, "Updating context stream notification.");
        NotificationManagerCompat.from(context).notify(WEAR_NOTIFICATION_ID, contextStream);
    }

    private void cancelNotification() {
        //Remove the notification from the context stream
        NotificationManagerCompat.from(getApplicationContext()).cancel(WEAR_NOTIFICATION_ID);
    }
}
