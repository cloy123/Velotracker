package com.govnokoder.velotracker.BL.Model;

import androidx.annotation.NonNull;

public class Time {
    public int Hours;
    public int Minutes;
    public int Seconds;

    public Time(int hours, int minutes, int seconds) {
        Hours = hours;
        Minutes = minutes;
        Seconds = seconds;
    }

    public Time() {
        Hours = 0;
        Minutes = 0;
        Seconds = 0;
    }

    public void addSecond(){
        if(Seconds == 59){
            Seconds =0;
            addMinute();
        } else {
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

    public long getAllSeconds() {
        return Seconds + (Minutes*60) + (Hours*3600);
    }

    public static Time getTimeFromSeconds(long seconds) {
        int horses = (int)(seconds / 3600);
        seconds -= horses*3600;
        int minutes = (int)(seconds/60);
        seconds -= minutes*60;
        int sec = (int)seconds;
        return new Time(horses, minutes, sec);
    }

    @NonNull
    @Override
    public String toString() {
        String stringTime = "";
        if(Hours != 0){
            if(Hours < 10){
                stringTime += "0" + Hours + ":";
            }else {
                stringTime += Hours + ":";
            }
        }
        if(Minutes < 10){
            stringTime += "0" + Minutes + ":";
        }else {
            stringTime += Minutes + ":";
        }
        if (Seconds < 10) {
            stringTime += "0" + Seconds;
        }else {
            stringTime += Seconds;
        }
        return stringTime;
    }
}
