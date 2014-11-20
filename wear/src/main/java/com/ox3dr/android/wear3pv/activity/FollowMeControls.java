package com.ox3dr.android.wear3pv.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceManager;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.ServiceListener;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.event.Event;
import com.ox3dr.android.wear3pv.R;
import com.ox3dr.android.wear3pv.fragment.BTConnectActionFragment;
import com.ox3dr.android.wear3pv.fragment.FollowActionFragment;

public class FollowMeControls extends Activity implements ServiceListener, DroneListener {

    private final static String TAG = FollowMeControls.class.getSimpleName();

    private final Handler handler = new Handler();

    private Drone drone;
    private ServiceManager serviceMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_controls);

        FragmentGridPagerAdapter adapter = new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int column) {
                switch (column) {
                    default:
                    case 0:
                        return new BTConnectActionFragment();

                    case 1:
                        return new FollowActionFragment();
                }
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount(int i) {
                return 2;
            }
        };

        GridViewPager viewPager = (GridViewPager) findViewById(R.id.grid_view_pager);
        viewPager.setAdapter(adapter);

        serviceMgr = new ServiceManager(getApplicationContext());
        drone = new Drone(serviceMgr, handler);
    }

    public Drone getDrone(){
        return drone;
    }

    @Override
    public void onStart() {
        super.onStart();
        serviceMgr.connect(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        this.drone.destroy();
        serviceMgr.disconnect();
    }

    @Override
    public void onServiceConnected() {
        showUser("Service connected.");
        this.drone.start();
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onServiceInterrupted() {
        showUser("Service interrupted.");
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {
        showUser("Connection failed: " + result.getErrorMessage());
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        if(Event.EVENT_CONNECTED.equals(event)){
            showUser("Connected!");
        }
        else if(Event.EVENT_DISCONNECTED.equals(event)){
            showUser("Disconnected!");
        }
        else if(Event.EVENT_FOLLOW_START.equals(event)){
            showUser("Follow-Me started.");
        }
        else if(Event.EVENT_FOLLOW_STOP.equals(event)){
            showUser("Follow-Me stopped.");
        }
        else if(Event.EVENT_FOLLOW_UPDATE.equals(event)){
            Log.d(TAG, "Follow me update.");
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    protected void showUser(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
