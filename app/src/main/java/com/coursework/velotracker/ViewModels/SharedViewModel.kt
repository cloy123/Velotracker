package com.coursework.velotracker.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coursework.velotracker.BL.Model.Training.ParcelableTraining

class SharedViewModel(): ViewModel() {
    var parcelableTraining:MutableLiveData<ParcelableTraining> = MutableLiveData<ParcelableTraining>()
    fun sendMessage(parcelableTraining: ParcelableTraining){
        this.parcelableTraining.value = parcelableTraining
    }
}