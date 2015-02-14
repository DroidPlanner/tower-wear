package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.dp.wear.lib.utils.AppPreferences;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager.GoogleApiClientTask;

/**
 * Created by fhuya on 1/2/15.
 */
abstract class BaseActivity extends Activity implements DataApi.DataListener {

    private final static String TAG = BaseActivity.class.getSimpleName();

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{Wearable.API};

    private final Handler handler = new Handler();

    private GoogleApiClientManager apiClientMgr;
    protected AppPreferences appPrefs;
    protected LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();
        apiClientMgr = new GoogleApiClientManager(context, handler, apisList);
        appPrefs = new AppPreferences(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);

        apiClientMgr.start();
        apiClientMgr.addTask(new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                Wearable.DataApi.addListener(getGoogleApiClient(), BaseActivity.this);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        apiClientMgr.addTask(new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                Wearable.DataApi.removeListener(getGoogleApiClient(), BaseActivity.this);
            }
        });
        apiClientMgr.stopSafely();
    }

    @Override
    public final void onDataChanged(DataEventBuffer dataEvents){
        for(DataEvent event: dataEvents) {
            final int eventType = event.getType();
            final DataItem dataItem = event.getDataItem();
            onDataItemReceived(dataItem, eventType);
        }

        dataEvents.release();
    }

    protected abstract void onVehicleDataUpdated(String dataType, byte[] eventData);

    protected void reloadVehicleData(String dataType){
        final String dataPath = WearUtils.VEHICLE_DATA_PREFIX + dataType;
        apiClientMgr.addTask(new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final Uri dataItemUri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(dataPath)
                        .build();

                Wearable.DataApi.getDataItems(getGoogleApiClient(), dataItemUri)
                        .setResultCallback(new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItemBuffer) {
                                final int dataCount = dataItemBuffer.getCount();
                                for(int i = 0; i < dataCount; i++) {
                                    final DataItem dataItem = dataItemBuffer.get(i);
                                    if (dataItem != null)
                                        onDataItemReceived(dataItem, DataEvent.TYPE_CHANGED);
                                }

                                dataItemBuffer.release();
                            }
                        });
            }
        });
    }

    private void onDataItemReceived(DataItem dataItem, int eventType){
        final Uri dataUri = dataItem.getUri();
        final String dataPath = dataUri.getPath();

        if (dataPath.startsWith(WearUtils.VEHICLE_DATA_PREFIX)) {
            final String dataType = dataPath.replace(WearUtils.VEHICLE_DATA_PREFIX, "");

            final byte[] eventData;
            if(eventType == DataEvent.TYPE_DELETED)
                eventData = null;
            else{
                eventData = dataItem.getData();
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    onVehicleDataUpdated(dataType, eventData);
                }
            });
        }
    }
}
