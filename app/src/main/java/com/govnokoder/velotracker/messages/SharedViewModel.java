package com.govnokoder.velotracker.messages;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.govnokoder.velotracker.BL.ParcelableTraining;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<ParcelableTraining> message = new MutableLiveData<ParcelableTraining>();
    public void sendMessage(ParcelableTraining parcelableTraining){
        message.setValue(parcelableTraining);
    }
}
