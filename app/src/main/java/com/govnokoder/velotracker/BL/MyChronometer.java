package com.govnokoder.velotracker.BL;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.govnokoder.velotracker.BL.Model.Time;

public class MyChronometer extends androidx.appcompat.widget.AppCompatTextView{

    Time time = new Time(0,0,0);
    boolean isRunning = false;
    boolean isPause = true;
    private String stringTime = "00:00";
    OnTickListenerInterface onTickListenerInterface;

    public void setOnTickListenerInterface(OnTickListenerInterface onTickListenerInterface){
        this.onTickListenerInterface = onTickListenerInterface;
    }

    CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if(!isPause){
                time.addSecond();
            }
            stringTime = "";
            if(time.Hours != 0){
                if(time.Hours < 10){
                    stringTime += "0" + time.Hours + ":";
                }else {
                    stringTime += time.Hours + ":";
                }
            }
            if(time.Minutes < 10){
                stringTime += "0" + time.Minutes + ":";
            }else {
                stringTime += time.Minutes + ":";
            }
            if (time.Seconds < 10) {
                stringTime += "0" + time.Seconds;
            }else {
                stringTime += time.Seconds;
            }
            onTickListenerInterface.OnTick(time);
            setText(stringTime);
        }

        @Override
        public void onFinish() {
                countDownTimer.start();
        }
    };

    public MyChronometer(@NonNull Context context) {
        super(context);
    }

    public MyChronometer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyChronometer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTime(Time time){
        this.time = time;
    }

    public Time getTime(){
        return  time;
    }

    public void Start(){
        if(!isRunning){
            countDownTimer.start();
        }
        isPause = false;
        isRunning = true;
    }

    public void Pause(){
        isPause = true;
    }

    public void Resume(){
        isPause = false;
    }

    public void Stop(){
        countDownTimer.cancel();
        isPause = true;
        isRunning = false;
    }

    public interface OnTickListenerInterface{
        public void OnTick(Time time);
    }

}


