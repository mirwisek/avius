package com.gesture.avius2.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gesture.avius2.App
import kotlinx.coroutines.launch

class StartViewModel(app: Application): AndroidViewModel(app) {

    private val repository = (app as App).repository
    val themeColor = repository.settings?.themeColor ?: ""

    val oldStatus = MutableLiveData<Int>()
    val thumbStatus = MutableLiveData<Int>()
    val progressBar = MutableLiveData(0)
    val hasValueChanged = MutableLiveData(false)

    // If progress exceeds beyond 80% there is no going back
    val isCancelable = MutableLiveData(true)

    val handDetectedLastTimestamp = MutableLiveData<Long>()


    fun tick() {
        viewModelScope.launch {
            if(progressBar.value!! > 25) {
                isCancelable.value = false
            }
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