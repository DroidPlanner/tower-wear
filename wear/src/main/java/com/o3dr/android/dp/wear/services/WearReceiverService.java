package com.o3dr.android.dp.wear.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.DataEventBuffer;
import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.ContextStreamActivity;
import com.o3dr.android.dp.wear.lib.services.WearRelayService;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearReceiverService extends WearRelayService {

    private static final int WEAR_NOTIFICATION_ID = 101;

    public static final String ACTION_SHOW_CONTEXT_STREAM = WearUtils.PACKAGE_NAME + ".action.SHOW_CONTEXT_STREAM";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            final String action = intent.getAction();
            if(action != null){
                switch(action){
                    case ACTION_SHOW_CONTEXT_STREAM:
                        updateContextStreamNotification();
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

    private void updateContextStreamNotification(){
        final Context context = getApplicationContext();
        final Resources res = getResources();

        final Intent displayIntent = new Intent(context, ContextStreamActivity.class);
        final PendingIntent displayPI = PendingIntent.getActivity(context, 0, displayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setDisplayIntent(displayPI)
                .setCustomSizePreset(Notification.WearableExtender.SIZE_DEFAULT);

        //Connection action
        final CharSequence connectTitle = "Connect";
        final Intent connectIntent = new Intent(context, WearReceiverService.class)
                .setAction(WearUtils.ACTION_CONNECT);
        final PendingIntent connectPI = PendingIntent.getService(context, 0, connectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action connectAction = new NotificationCompat.Action(R.drawable.ic_follow_active,
                connectTitle, connectPI);

        extender.addAction(connectAction);

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
