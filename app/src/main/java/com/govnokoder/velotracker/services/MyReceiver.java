package com.govnokoder.velotracker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.govnokoder.velotracker.BL.ParcelableTraining;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ParcelableTraining parcelableTraining = intent.getParcelableExtra(LocationService.EXTRA_PARCELABLE_TRAINING);
        if (parcelableTraining != null) {
        }
    }
}
