package com.coursework.velotracker.Messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coursework.velotracker.BL.Model.ParcelableTraining

class SharedViewModel: ViewModel() {
    var messagesParcelableTraining:MutableLiveData<ParcelableTraining> = MutableLiveData<ParcelableTraining>()
    fun sendMessage(parcelableTraining: ParcelableTraining){
        messagesParcelableTraining.value = parcelableTraining
    }
}