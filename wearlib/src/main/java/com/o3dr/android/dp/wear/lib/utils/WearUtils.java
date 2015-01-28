package com.o3dr.android.dp.wear.lib.utils;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearUtils {

    public static final String TOWER_APP_ID = "org.droidplanner.android";

    private static final String TAG = WearUtils.class.getSimpleName();

    public static final String PACKAGE_NAME = "com.o3dr.android.dp.wear";

    private static final String ROOT_PATH = "/dp/wear";

    public static final String VEHICLE_DATA_PREFIX = ROOT_PATH + "/vehicle_data/";

    public static final String ACTION_PREFIX = ROOT_PATH + "/action";

    private static final String PREFERENCE_PREFIX = ROOT_PATH + "/app_prefs";

    /* List of supported actions */

    /**
     * Used to request connection to the vehicle.
     */
    public static final String ACTION_CONNECT = ACTION_PREFIX + "/connect";

    /**
     * Used to request disconnection from the vehicle.
     */
    public static final String ACTION_DISCONNECT = ACTION_PREFIX + "/disconnect";

    public static final String ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION = ACTION_PREFIX +
            "/show_context_stream_notification";

    public static final String ACTION_ARM = ACTION_PREFIX + "/arm";
    public static final String ACTION_TAKE_OFF = ACTION_PREFIX + "/take_off";
    public static final String ACTION_DISARM = ACTION_PREFIX + "/disarm";
    public static final String ACTION_OPEN_PHONE_APP = ACTION_PREFIX + "/open_phone_app";
    public static final String ACTION_OPEN_WEAR_APP = ACTION_PREFIX + "/open_wear_app";
    public static final String ACTION_REFRESH_VEHICLE_ATTRIBUTE = ACTION_PREFIX + "/refresh_vehicle_attribute";
    public static final String ACTION_CHANGE_VEHICLE_MODE = ACTION_PREFIX + "/change_vehicle_mode";
    public static final String ACTION_START_FOLLOW_ME = ACTION_PREFIX + "/start_follow_me";
    public static final String ACTION_CHANGE_FOLLOW_ME_TYPE = ACTION_PREFIX + "/change_follow_me_type";
    public static final String ACTION_STOP_FOLLOW_ME = ACTION_PREFIX + "/stop_follow_me";
    public static final String ACTION_SET_GUIDED_ALTITUDE = ACTION_PREFIX + "/set_guided_altitude";
    public static final String ACTION_SET_FOLLOW_ME_RADIUS = ACTION_PREFIX + "/set_follow_me_radius";

    /* List of app preferences */
    public static final String PREF_IS_HDOP_ENABLED = PREFERENCE_PREFIX + "/hdop_enabled";
    public static final String PREF_NOTIFICATION_PERMANENT = PREFERENCE_PREFIX + "/permanent_notification";
    public static final String PREF_SCREEN_STAYS_ON = PREFERENCE_PREFIX + "/screen_stays_on";
    public static final String PREF_UNIT_SYSTEM = PREFERENCE_PREFIX + "/unit_system";

    /**
     * Asynchronously send a message using the Wearable.MessageApi api to connected wear nodes.
     *
     * @param apiClientMgr google api client manager
     * @param msgPath      non-null path for the message
     * @param msgData      optional message data
     * @return true if the message task was successfully queued.
     */
    public static boolean asyncSendMessage(GoogleApiClientManager apiClientMgr,
                                           final String msgPath, final byte[] msgData) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final GoogleApiClient apiClient = getGoogleApiClient();

                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(apiClient)
                        .await();

                for (Node node : nodes.getNodes()) {
                    Log.d(TAG, "Sending message to " + node.getDisplayName());
                    final MessageApi.SendMessageResult result = Wearable.MessageApi
                            .sendMessage(apiClient, node.getId(), msgPath, msgData)
                            .await();

                    final Status status = result.getStatus();
                    if (!status.isSuccess()) {
                        Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                    }
                }
            }
        });
    }

    /**
     * Asynchronously push/update a data item using the Wearable.DataApi api to connected wear
     * nodes.
     * @param apiClientMgr google api client manager
     * @param path non-null path
     * @param dataMapBundle non-null data bundle
     * @return true if the task was successfully queued.
     */
    public static boolean asyncPutDataItem(GoogleApiClientManager apiClientMgr,
                                           final String path, final Bundle dataMapBundle) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final PutDataMapRequest dataMap = PutDataMapRequest.create(path);
                dataMap.getDataMap().putAll(DataMap.fromBundle(dataMapBundle));
                PutDataRequest request = dataMap.asPutDataRequest();
                final DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(getGoogleApiClient(), request)
                        .await();

                final Status status = result.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                }
            }
        });
    }

    /**
     * Asynchronously push/update a data item using the Wearable.DataApi api to connected wear
     * nodes.
     * @param apiClientMgr google api client manager
     * @param path non-null path
     * @param data non-null data payload
     * @return true if the task was successfully queued.
     */
    public static boolean asyncPutDataItem(GoogleApiClientManager apiClientMgr,
                                           final String path, final byte[] data) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final PutDataRequest request = PutDataRequest.create(path);
                request.setData(data);
                final DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(getGoogleApiClient(), request)
                        .await();

                final Status status = result.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                }
            }
        });
    }
}
