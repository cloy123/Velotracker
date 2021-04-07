package com.coursework.velotracker.BL.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

public class LineListJ extends ArrayList<LatLng> implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this);
    }

    public LineListJ(){
        super();
    }

    protected LineListJ(Parcel in){
        in.readTypedList(this, LatLng.CREATOR);
    }

    public static final Parcelable.Creator<LineListJ> CREATOR = new Parcelable.Creator<LineListJ>() {
        public LineListJ createFromParcel(Parcel in) {
            return new LineListJ(in);
        }

        public LineListJ[] newArray(int size) {
            return new LineListJ[size];
        }
    };


}
