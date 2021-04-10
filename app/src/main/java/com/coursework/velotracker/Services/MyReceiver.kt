package com.coursework.velotracker.Services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coursework.velotracker.BL.Model.ParcelableTraining

class MyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val parcelableTraining: ParcelableTraining? = intent.getParcelableExtra(LocationService.EXTRA_PARCELABLE_TRAINING)
    }
}