package com.o3dr.android.dp.wear.lib.utils;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearUtils {

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
}
