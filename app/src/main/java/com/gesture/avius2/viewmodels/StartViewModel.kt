package com.gesture.avius2.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class StartViewModel: ViewModel() {

    val oldStatus = MutableLiveData<Int>()
    val thumbStatus = MutableLiveData<Int>()
    val progressBar = MutableLiveData(0)
    val hasValueChanged = MutableLiveData(false)

    val handDetectedLastTimestamp = MutableLiveData<Long>()


    fun tick() {
        viewModelScope.launch {
            progressBar.value = progressBar.value!!.plus(3)
        }
    }

    fun updateThumbStatus(thumbDir: Int) {
        viewModelScope.launch {
            // If new value is different, then store in variable so counter can reset progress
            if(oldStatus.value != thumbDir)
                hasValueChanged.value = true
            oldStatus.value = thumbStatus.value
            thumbStatus.value = thumbDir
        }
    }

}