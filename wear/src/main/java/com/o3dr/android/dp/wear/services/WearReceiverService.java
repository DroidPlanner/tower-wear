package com.o3dr.android.dp.wear.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.ConfirmationActivity;

import com.google.android.gms.wearable.DataEventBuffer;
import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.ContextStreamActivity;
import com.o3dr.android.dp.wear.activities.FlightModesSelectionActivity;
import com.o3dr.android.dp.wear.activities.HomeActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.util.ParcelableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearReceiverService extends WearRelayService {

    private static final int WEAR_NOTIFICATION_ID = 101;
    public static final String EXTRA_CURRENT_FLIGHT_MODE = "extra_current_flight_mode";
    public static final String EXTRA_VEHICLE_STATE = "extra_vehicle_state";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            final String action = intent.getAction();
            if(action != null){
                switch(action){
                    case WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION:
                        updateContextStreamNotification(new State());
                        break;

                    case WearUtils.ACTION_OPEN_PHONE_APP:
                        startActivity(new Intent(getApplicationContext(), ConfirmationActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                        ConfirmationActivity.OPEN_ON_PHONE_ANIMATION));
                        /* FALL - THROUGH */
                    case WearUtils.ACTION_ARM:
                    case WearUtils.ACTION_DISARM:
                    case WearUtils.ACTION_CONNECT:
                    case WearUtils.ACTION_DISCONNECT:
                    case WearUtils.ACTION_TAKE_OFF:
                        sendMessage(action, null);
                        break;
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onEventReceived(String attributeEvent, byte[] data){
        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        switch(attributeEvent){
            case AttributeEvent.STATE_CONNECTED:
                lbm.sendBroadcast(new Intent(attributeEvent));
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                lbm.sendBroadcast(new Intent(attributeEvent));
                break;
        }
    }

    @Override
    protected void onActionRequested(String actionPath, byte[] actionData){
        switch(actionPath){
            case WearUtils.ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION:
                State vehicleState = null;
                if(actionData != null){
                    vehicleState = ParcelableUtils.unmarshall(actionData, State.CREATOR);
                }
                if(vehicleState == null)
                    vehicleState = new State();
                updateContextStreamNotification(vehicleState);
                break;
        }
    }

    private void updateContextStreamNotification(State vehicleState){
        final Context context = getApplicationContext();
        final Resources res = getResources();

        //Head card display
        final Intent displayIntent = new Intent(context, ContextStreamActivity.class)
                .putExtra(EXTRA_VEHICLE_STATE, vehicleState);
        final PendingIntent displayPI = PendingIntent.getActivity(context, 0, displayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(res, R.drawable.wear_notification_bg))
                .setDisplayIntent(displayPI)
                .setCustomSizePreset(Notification.WearableExtender.SIZE_DEFAULT);

        final List<NotificationCompat.Action> actionsList = new ArrayList<>();

        //Flight action cards
        final boolean isConnected = vehicleState != null && vehicleState.isConnected();

        if(isConnected){
            if(vehicleState.isFlying()){
                final Intent flightModesIntent = new Intent(context, FlightModesSelectionActivity.class).putExtra
                        (EXTRA_CURRENT_FLIGHT_MODE, (Parcelable)vehicleState.getVehicleMode());
                final PendingIntent flightModesPI = PendingIntent.getActivity(context, 0, flightModesIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                final NotificationCompat.Action flightModeAction = new NotificationCompat.Action(R.drawable
                        .ic_flight_white_48dp, getText(R.string.flight_modes), flightModesPI);

                actionsList.add(flightModeAction);
            }
            else if(vehicleState.isArmed()){
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
            }
            else{
                final Intent armIntent = new Intent(context, WearReceiverService.class).setAction(WearUtils.ACTION_ARM);
                final PendingIntent armPI = PendingIntent.getService(context, 0, armIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                final NotificationCompat.Action armAction = new NotificationCompat.Action(R.drawable.ic_arm,
                        getText(R.string.arm), armPI);

                actionsList.add(armAction);

                final Intent disconnectIntent = new Intent(context, WearReceiverService.class).setAction(WearUtils
                        .ACTION_DISCONNECT);
                final PendingIntent disconnectPI = PendingIntent.getService(context, 0, disconnectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                final NotificationCompat.Action disconnectAction = new NotificationCompat.Action(R.drawable
                        .ic_settings_power_white_48dp, getText(R.string.disconnect), disconnectPI);

                actionsList.add(disconnectAction);
            }
        }
        else{
            final Intent connectIntent = new Intent(context, WearReceiverService.class)
                    .setAction(WearUtils.ACTION_CONNECT);
            final PendingIntent connectPI = PendingIntent.getService(context, 0, connectIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Action connectAction = new NotificationCompat
                    .Action(R.drawable.ic_settings_power_white_48dp, getText(R.string.connect), connectPI);

            actionsList.add(connectAction);
        }

        //Open Wear app card.
        final Intent openWearAppIntent = new Intent(context, HomeActivity.class).putExtra(EXTRA_VEHICLE_STATE,
                vehicleState);
        final PendingIntent openWearAppPI = PendingIntent.getActivity(context, 0, openWearAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action openWearAppAction = new NotificationCompat.Action(R.drawable
                .ic_fullscreen_white_48dp, getText(R.string.open_app), openWearAppPI);

        actionsList.add(openWearAppAction);

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
                .extend(extender)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        NotificationManagerCompat.from(context).notify(WEAR_NOTIFICATION_ID, contextStream);
    }

    private void cancelNotification(){
        //Remove the notification from the context stream
        NotificationManagerCompat.from(getApplicationContext()).cancel(WEAR_NOTIFICATION_ID);
    }
}
