package com.coursework.velotracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("theme", MODE_PRIVATE)
        if(sharedPreferences.contains("dark")){
            if(sharedPreferences.getBoolean("dark", false)){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        else if(sharedPreferences.contains("light")){
            if (sharedPreferences.getBoolean("light", false)){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.edit().putBoolean("light", true).apply()
        }
    }
}