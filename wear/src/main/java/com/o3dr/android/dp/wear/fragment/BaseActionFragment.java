package com.o3dr.android.dp.wear.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.activities.WearUIActivity;
import com.o3dr.android.dp.wear.lib.utils.WearFollowState;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

/**
 * Template for the action views used in the app.
 */
public abstract class BaseActionFragment extends Fragment implements View.OnClickListener {

    private final static IntentFilter intentFilter = new IntentFilter();
    static {
        intentFilter.addAction(AttributeType.STATE);
        intentFilter.addAction(AttributeType.GUIDED_STATE);
        intentFilter.addAction(AttributeType.FOLLOW_STATE);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case AttributeType.FOLLOW_STATE:
                case AttributeType.GUIDED_STATE:
                    updateLayout();
                    break;
            }
        }
    };

    private Vibrator vibrator;
    private WearUIActivity parentActivity;

    private CircledImageView faveImage;
    private TextView faveLabel;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof WearUIActivity)) {
            throw new IllegalStateException("Parent activity must be an instance of " + WearUIActivity.class.getName());
        }

        parentActivity = (WearUIActivity) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        parentActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        faveImage = (CircledImageView) view.findViewById(R.id.listing_action_image);
        faveLabel = (TextView) view.findViewById(R.id.listing_action_label);
        updateLayout();

        LocalBroadcastManager.getInstance(parentActivity.getApplicationContext()).registerReceiver(broadcastReceiver,
                intentFilter);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        LocalBroadcastManager.getInstance(parentActivity.getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    private void updateLayout(){
        if (faveImage != null) {
            faveImage.setImageResource(getActionImageResource());
            faveImage.setOnClickListener(this);
        }

        if (faveLabel != null) {
            faveLabel.setText(getActionLabel());
        }
    }

    protected Context getContext(){
        return parentActivity.getApplicationContext();
    }

    protected State getVehicleState(){
        return parentActivity.getVehicleState();
    }

    protected WearFollowState getVehicleFollowState(){
        return parentActivity.getFollowState();
    }

    protected GuidedState getVehicleGuidedState(){
        return parentActivity.getGuidedState();
    }

    protected abstract int getActionImageResource();

    protected abstract CharSequence getActionLabel();

    protected abstract void onActionClicked();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.listing_action_image:
                vibrator.vibrate(20);
                onActionClicked();
                break;
        }
    }

}
