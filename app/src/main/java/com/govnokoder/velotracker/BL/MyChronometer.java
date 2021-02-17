package com.govnokoder.velotracker.BL;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.govnokoder.velotracker.BL.Model.Time;

public class MyChronometer extends androidx.appcompat.widget.AppCompatTextView {

    Time time = new Time(0,0,0);
    boolean isPause = true;
    CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            time.addSecond();
            setText(time.toString());
        }

        @Override
        public void onFinish() {
            if(!isPause){
                countDownTimer.start();
            }
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
        if(isPause){
            countDownTimer.start();
        }
    }

    public void Stop(){
        if(!isPause){
            countDownTimer.cancel();
        }
    }

}
