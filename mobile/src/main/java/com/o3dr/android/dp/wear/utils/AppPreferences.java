package com.o3dr.android.dp.wear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.o3dr.android.dp.wear.R;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.StreamRates;

/**
 * Provides structured access to the app preferences.
 */
public class AppPreferences {

    /*
    * Default preference values
    */
    public static final boolean DEFAULT_USAGE_STATISTICS = true;
    public static final String DEFAULT_CONNECTION_TYPE = String.valueOf(ConnectionType.TYPE_USB);
    private static final String DEFAULT_USB_BAUD_RATE = "57600";
    private static final String DEFAULT_TCP_SERVER_IP = "192.168.40.100";
    private static final String DEFAULT_TCP_SERVER_PORT = "5763";
    private static final String DEFAULT_UDP_SERVER_PORT = "14550";

    public final SharedPreferences prefs;
    private final Context context;

    public AppPreferences(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * @return true if google analytics reporting is enabled.
     */
    public boolean isUsageStatisticsEnabled() {
        return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key), DEFAULT_USAGE_STATISTICS);
    }

    public void setConnectionParameterType(int connectionType){
        prefs.edit().putString(context.getString(R.string.pref_connection_type_key), String.valueOf(connectionType)).apply();
    }
    public int getConnectionParameterType() {
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_connection_type_key), DEFAULT_CONNECTION_TYPE));
    }

    public String getBluetoothDeviceAddress() {
        return prefs.getString(context.getString(R.string.pref_bluetooth_device_address_key), null);
    }

    public void setBluetoothDeviceAddress(String newAddress) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_bluetooth_device_address_key), newAddress).apply();
    }

    public void setUsbBaudRate(int baudRate){
        prefs.edit().putString(context.getString(R.string.pref_baud_type_key), String.valueOf(baudRate)).apply();
    }

    public int getUsbBaudRate(){
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_baud_type_key), DEFAULT_USB_BAUD_RATE));
    }

    public void setTcpServerIp(String serverIp){
        prefs.edit().putString(context.getString(R.string.pref_server_ip_key), serverIp).apply();
    }

    public String getTcpServerIp(){
        return prefs.getString(context.getString(R.string.pref_server_ip_key), DEFAULT_TCP_SERVER_IP);
    }

    public void setTcpServerPort(int serverPort){
        prefs.edit().putString(context.getString(R.string.pref_server_port_key), String.valueOf(serverPort)).apply();
    }

    public int getTcpServerPort(){
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_server_port_key), DEFAULT_TCP_SERVER_PORT));
    }

    public void setUdpServerPort(int serverPort){
        prefs.edit().putString(context.getString(R.string.pref_udp_server_port_key), String.valueOf(serverPort)).apply();
    }

    public int getUdpServerPort(){
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_udp_server_port_key), DEFAULT_UDP_SERVER_PORT));
    }

    public StreamRates getStreamRates() {
        StreamRates rates = new StreamRates();

        rates.setExtendedStatus(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_ext_stat", "2")));
        rates.setExtra1(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1", "2")));
        rates.setExtra2(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2", "2")));
        rates.setExtra3(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3", "2")));
        rates.setPosition(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_position", "2")));
        rates.setRcChannels(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_rc_channels", "2")));
        rates.setRawSensors(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_sensors", "2")));
        rates.setRawController(Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_controller", "2")));
        return rates;
    }

    public boolean getLiveUploadEnabled() {
        // FIXME: Disabling live upload as it often causes the app to freeze on
        // disconnect.
        // return
        // prefs.getBoolean(context.getString(R.string.pref_live_upload_enabled_key),
        // false);
        return false;
    }

    public String getDroneshareLogin() {
        return prefs.getString(context.getString(R.string.pref_dshare_username_key), "").trim();
    }

    public void setDroneshareLogin(String b) {
        prefs.edit().putString(context.getString(R.string.pref_dshare_username_key), b.trim())
                .apply();
    }

    public String getDroneshareEmail() {
        return prefs.getString("dshare_email", "").trim();
    }

    public void setDroneshareEmail(String b) {
        prefs.edit().putString("dshare_email", b.trim()).apply();
    }

    public String getDronesharePassword() {
        return prefs.getString(context.getString(R.string.pref_dshare_password_key), "").trim();
    }

    public void setDronesharePassword(String b) {
        prefs.edit().putString(context.getString(R.string.pref_dshare_password_key), b.trim()).apply();
    }

    public boolean getDroneshareEnabled() {
        return !TextUtils.isEmpty(getDroneshareLogin()) && !TextUtils.isEmpty(getDronesharePassword());
    }
}
