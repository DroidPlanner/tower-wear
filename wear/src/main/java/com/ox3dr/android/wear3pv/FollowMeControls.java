package com.ox3dr.android.wear3pv;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.o3dr.android.client.DPApiCallback;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceListener;
import com.o3dr.android.client.ServiceManager;

public class FollowMeControls extends Activity implements ServiceListener {

    private TextView mTextView;

    private Drone drone;
    private ServiceManager serviceMgr;
    private DPApiCallback apiCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_controls);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

            }
        });

        final Context context = getApplicationContext();
        serviceMgr = new ServiceManager(context);
        drone = new Drone(context, serviceMgr);
        apiCallback = new DPApiCallback(context);
    }

    @Override
    public void onStart(){
        super.onStart();
        serviceMgr.connect(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        serviceMgr.disconnect(this);
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }
}
