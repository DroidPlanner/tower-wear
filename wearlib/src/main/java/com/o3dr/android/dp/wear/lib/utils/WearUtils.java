package com.o3dr.android.dp.wear.lib.utils;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearUtils {

    private static final String TAG = WearUtils.class.getSimpleName();

    public static final String PACKAGE_NAME = "com.o3dr.android.dp.wear";

    private static final String ROOT_PATH = "/dp/wear";

    public static final String EVENT_PREFIX = ROOT_PATH + "/event/";

    public static final String ACTION_PREFIX = ROOT_PATH + "/action";

    /* List of supported actions */

    /**
     * Used to request connection to the vehicle.
     */
    public static final String ACTION_CONNECT = ACTION_PREFIX + "/connect";

    /**
     * Used to request disconnection from the vehicle.
     */
    public static final String ACTION_DISCONNECT = ACTION_PREFIX + "/disconnect";

    /**
     * Used to request drone attribute. The desired attribute should be passed in the message event data.
     */
    public static final String ACTION_REQUEST_ATTRIBUTE = ACTION_PREFIX + "/request_attribute";

    public static final String ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION = ACTION_PREFIX +
            "/show_context_stream_notification";

    public static final String ACTION_ARM = ACTION_PREFIX + "/arm";
    public static final String ACTION_TAKE_OFF = ACTION_PREFIX + "/take_off";
    public static final String ACTION_DISARM = ACTION_PREFIX + "/disarm";
    public static final String ACTION_OPEN_PHONE_APP = ACTION_PREFIX + "/open_phone_app";
    public static final String ACTION_OPEN_WEAR_APP = ACTION_PREFIX + "/open_wear_app";

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
}
