package com.o3dr.android.dp.wear.lib.utils;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

/**
 * Created by Fredia Huya-Kouadio on 1/28/15.
 */
public class WearFollowState extends FollowState {

    private double radius = 2;

    public WearFollowState(FollowState followState){
        super(followState.getState(), followState.getMode(), followState.getParams());
        loadFromParams();
    }

    private void loadFromParams(){
        Bundle params = super.getParams();
        if(params != null){
            radius = params.getDouble(FollowType.EXTRA_FOLLOW_RADIUS, 2);
        }
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Bundle getParams(){
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(getState());

        FollowType mode = getMode();
        dest.writeInt(mode == null ? -1 : mode.ordinal());

        dest.writeDouble(radius);
    }

    private WearFollowState(Parcel in){
        setState(in.readInt());

        int tmpMode = in.readInt();
        setMode(tmpMode == -1 ? null : FollowType.values()[tmpMode]);

        radius = in.readDouble();
    }

    public static final Parcelable.Creator<WearFollowState> CREATOR = new Parcelable.Creator<WearFollowState>() {
        public WearFollowState createFromParcel(Parcel source) {
            return new WearFollowState(source);
        }

        public WearFollowState[] newArray(int size) {
            return new WearFollowState[size];
        }
    };
}
