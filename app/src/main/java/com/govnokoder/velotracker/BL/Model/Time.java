package com.govnokoder.velotracker.BL.Model;

import androidx.annotation.NonNull;

public class Time {
    public int Hours;
    public int Minutes;
    public int Seconds;

    private String HoursStr;
    private String MinutesStr;
    private String SecondsStr;

    public Time(int hours, int minutes, int seconds)
    {
        Hours = hours;
        Minutes = minutes;
        Seconds = seconds;
    }

    public Time()
    {
        Hours = 0;
        Minutes = 0;
        Seconds = 0;
    }

    public void addSecond(){
        if(Seconds == 59){
            Seconds =0;
            addMinute();
        }
        else {
            Seconds +=1;
        }
    }

    private void addMinute(){
        if(Minutes == 59){
            Minutes = 0;
            Hours +=1;
        } else{
            Minutes += 1;
        }
    }

    public int getAllSeconds() {
        return Seconds + (Minutes*60) + (Hours*3600);
    }

    public static Time getTimeFromSeconds(int seconds) {
        int horses = seconds / 3600;
        int minutes = (seconds - horses*3600)/60;
        int sec = seconds - minutes*60;
        return new Time(horses, minutes, sec);
    }

    @NonNull
    @Override
    public String toString() {

        HoursStr = Integer.toString(Hours);
        MinutesStr = Integer.toString(Minutes);
        SecondsStr = Integer.toString(Seconds);
        if(Minutes < 10) {
            MinutesStr = "0" + Minutes;
        }
        if(Seconds < 10){
            SecondsStr = "0" + Seconds;
        }

        return Hours + ":" + Minutes + ":" + Seconds;
    }


    public static Time getSecondsFromDurationString(String value){

        String [] parts = value.split(":");

        // Wrong format, no value for you.
        if(parts.length < 2 || parts.length > 3)
            return new Time();

        int seconds = 0, minutes = 0, hours = 0;

        if(parts.length == 2){
            seconds = Integer.parseInt(parts[1]);
            minutes = Integer.parseInt(parts[0]);
        }
        else if(parts.length == 3){
            seconds = Integer.parseInt(parts[2]);
            minutes = Integer.parseInt(parts[1]);
            hours = Integer.parseInt(parts[0]);
        }

        return new Time(hours, minutes, seconds);
    }


}
