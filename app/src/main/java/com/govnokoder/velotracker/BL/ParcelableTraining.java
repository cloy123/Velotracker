package com.govnokoder.velotracker.BL;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.govnokoder.velotracker.BL.Model.Time;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ParcelableTraining implements Parcelable {

    public Time time;
    public double distance;
    public double maxSpeed;
    public double averageSpeed;
    private List<LineList> _lines;
    public List<LineList> getLines() { return _lines; };
    public void setLines(List<LineList> lines) { _lines = lines; }
    public LineList currentLine;
    public long maxHeight;
    public long minHeight;
    public long averageHeight;
    public boolean isRunning;
    public Location originLocation;
    public long height;

    public ParcelableTraining(Time time, double distance, double maxSpeed,
                              double averageSpeed, List<LineList> lines,
                              LineList currentLine, long maxHeight, long height,
                              long minHeight, long averageHeight, boolean isRunning,
                              Location originLocation){
        this.time = time;
        this.distance = distance;
        this.maxSpeed = maxSpeed;
        this.averageSpeed = averageSpeed;
        setLines(lines);
        this.currentLine = currentLine;

        this.maxHeight = maxHeight;
        this.minHeight = minHeight;
        this.height = height;
        this.averageHeight = averageHeight;
        this.isRunning = isRunning;
        this.originLocation = originLocation;
    }

    protected ParcelableTraining(Parcel in) {
        distance = in.readDouble();
        maxSpeed = in.readDouble();
        averageSpeed = in.readDouble();
        maxHeight = in.readLong();
        minHeight = in.readLong();
        averageHeight = in.readLong();
        isRunning = in.readByte() != 0;
        originLocation = in.readParcelable(Location.class.getClassLoader());
        height = in.readLong();
        _lines = new ArrayList<>();
        in.readList(_lines, LineList.class.getClassLoader());
        currentLine = new LineList();
        in.readList(currentLine, LatLng.class.getClassLoader());
    }

    public static final Creator<ParcelableTraining> CREATOR = new Creator<ParcelableTraining>() {
        @Override
        public ParcelableTraining createFromParcel(Parcel in) {
            return new ParcelableTraining(in);
        }

        @Override
        public ParcelableTraining[] newArray(int size) {
            return new ParcelableTraining[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(distance);
        dest.writeDouble(maxSpeed);
        dest.writeDouble(averageSpeed);
        dest.writeLong(maxHeight);
        dest.writeLong(minHeight);
        dest.writeLong(averageHeight);
        dest.writeByte((byte) (isRunning ? 1 : 0));
        dest.writeParcelable(originLocation, flags);
        dest.writeLong(height);
        dest.writeList(_lines);
        dest.writeList(currentLine);
    }
}
