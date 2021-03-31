package com.govnokoder.velotracker.messages;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.govnokoder.velotracker.BL.ParcelableTraining;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<ParcelableTraining> messagesParcelableTraining = new MutableLiveData<ParcelableTraining>();
    public void sendMessage(ParcelableTraining parcelableTraining){
        messagesParcelableTraining.setValue(parcelableTraining);
    }
}
