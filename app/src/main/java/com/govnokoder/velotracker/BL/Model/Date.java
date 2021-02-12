package com.govnokoder.velotracker.BL.Model;

import androidx.annotation.NonNull;

import java.util.Calendar;

public class Date{
    public int Year;
    public int Month;
    public int Day;

    private String DayStr;
    private String MonthStr;
    private String YearStr;

    public Date(int day, int month, int year) {
        Year = year;
        Month = month;
        Day = day;
    }

    public Date(){
        Year = 0;
        Month = 0;
        Day = 0;
    }

    public void setCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();

        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH)+1;
        Day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @NonNull
    @Override
    public String toString() {

        DayStr = Integer.toString(Day);
        MonthStr = Integer.toString(Month);
        YearStr = Integer.toString(Year);

        if(Day < 10) {
            DayStr = "0" + Day;
        }

        if(Month < 10) {
            MonthStr = "0" + Month;
        }

        if(Year < 10) {
            YearStr = "000"+Year;
        }

        if(Year < 100) {
            YearStr = "00"+Year;
        }

        if(Year < 1000) {
            YearStr = "0"+Year;
        }

        return DayStr + "/" + MonthStr + "/" + YearStr;
    }
}
