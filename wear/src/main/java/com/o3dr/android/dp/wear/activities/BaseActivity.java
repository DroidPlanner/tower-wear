package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.o3dr.android.dp.wear.lib.utils.GoogleApiClientManager;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;

/**
 * Created by fhuya on 1/2/15.
 */
abstract class BaseActivity extends Activity implements DataApi.DataListener {

    private final static String TAG = BaseActivity.class.getSimpleName();

    private final Handler handler = new Handler();

    private GoogleApiClientManager apiClientMgr;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        apiClientMgr = new GoogleApiClientManager(getApplicationContext(), handler, Wearable.API);
    }

    @Override
    public void onStart(){
        super.onStart();
        apiClientMgr.start();
        apiClientMgr.addTask(apiClientMgr.new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                Wearable.DataApi.addListener(getGoogleApiClient(), BaseActivity.this);
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        apiClientMgr.addTask(apiClientMgr.new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                Wearable.DataApi.removeListener(getGoogleApiClient(), BaseActivity.this);
                apiClientMgr.stop();
            }
        });
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
        apiClientMgr.addTask(apiClientMgr.new GoogleApiClientTask() {
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
