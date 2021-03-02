package com.govnokoder.velotracker.messages;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.govnokoder.velotracker.BL.CurrentTraining;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<CurrentTraining> message = new MutableLiveData<CurrentTraining>();
    public void sendMessage(CurrentTraining currentTraining){
        message.setValue(currentTraining);
    }
}
