package com.govnokoder.velotracker.BL;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

public class LineList extends ArrayList<LatLng> implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this);
    }

    public LineList(){
        super();
    }

    protected LineList(Parcel in){
        in.readTypedList(this, LatLng.CREATOR);
    }

    public static final Parcelable.Creator<LineList> CREATOR = new Parcelable.Creator<LineList>() {
        public LineList createFromParcel(Parcel in) {
            return new LineList(in);
        }

        public LineList[] newArray(int size) {
            return new LineList[size];
        }
    };


}
