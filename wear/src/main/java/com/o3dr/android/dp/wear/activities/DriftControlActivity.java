package com.o3dr.android.dp.wear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.o3dr.android.dp.wear.R;
import com.o3dr.android.dp.wear.lib.utils.WearUtils;
import com.o3dr.android.dp.wear.services.WearReceiverService;
import com.o3dr.android.dp.wear.widgets.JoystickView;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import timber.log.Timber;

/**
 * Created by Toby on 8/7/2015.
 */
public class DriftControlActivity extends BaseActivity implements JoystickView.JoystickListener{

    private GestureDetectorCompat gestureDetector;
    private DismissOverlayView mDismissOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drift_control);
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.long_press_intro);
        mDismissOverlay.showIntroIfNecessary();
        gestureDetector = new GestureDetectorCompat(getApplicationContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                mDismissOverlay.show();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                startService(new Intent(getApplicationContext(), WearReceiverService.class)
                        .setAction(WearUtils.ACTION_DRIFT_STOP));
                return true;
            }
        });
        JoystickView joystickView = (JoystickView) findViewById(R.id.joystick);
        joystickView.setJoystickListener(this);
    }

    @Override
    protected void onVehicleDataUpdated(String dataType, byte[] eventData) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onJoystickEngaged(float x, float y) {
        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_DRIFT_CONTROL)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, new float[]{x,y}));
    }

    @Override
    public void onJoystickMoved(float x, float y) {
        startService(new Intent(getApplicationContext(), WearReceiverService.class)
                .setAction(WearUtils.ACTION_DRIFT_CONTROL)
                .putExtra(WearReceiverService.EXTRA_ACTION_DATA, new float[]{x,y}));
    }

    @Override
    public void onJoystickReleased(float x, float y) {
    }
}
