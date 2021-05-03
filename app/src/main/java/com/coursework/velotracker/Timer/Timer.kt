package com.coursework.velotracker.Timer

import android.os.CountDownTimer
import java.time.LocalTime

class Timer(onTickListener: OnTickListener){

    var isPaused = true
    var isLaunched = false
    var time:LocalTime = LocalTime.MIN

    var countDownTimer:CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            if(!isPaused){
                time = time.plusSeconds(1)
            }
            onTickListener.onTick(time)
        }

        override fun onFinish() {
            if(!isPaused){
                this.start()
            }
        }
    }

    fun start(){
        if(!isLaunched) {
            countDownTimer.start()
            isPaused = false
            isLaunched = true
        }
    }

    fun resume(){
            isPaused = false
    }

    fun pause(){
            isPaused = true
    }

    fun stop(){
        isLaunched = false
        isPaused = true
        countDownTimer.cancel()
    }

    interface OnTickListener{
        fun onTick(time:LocalTime)
    }
}