package com.ox3dr.android.wear3pv.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.client.DPApiCallback;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceListener;
import com.o3dr.android.client.ServiceManager;
import com.ox3dr.android.wear3pv.R;
import com.ox3dr.android.wear3pv.fragment.BTConnectActionFragment;
import com.ox3dr.android.wear3pv.fragment.FollowActionFragment;

public class FollowMeControls extends Activity implements ServiceListener {

    private Drone drone;
    private ServiceManager serviceMgr;
    private DPApiCallback apiCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_controls);

        FragmentGridPagerAdapter adapter = new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int column) {
                switch(column){
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
