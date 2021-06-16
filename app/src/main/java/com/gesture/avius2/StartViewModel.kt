package com.gesture.avius2

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

    private lateinit var handler: Handler

    private val progressRunnable = {
        progressBar.value = progressBar.value!!.plus(1)
        checkProgress()
    }

    private fun checkProgress() {
        if(progressBar.value!! % 10 == 0) {
            handler.removeCallbacksAndMessages(null)
        } else {
            handler.postDelayed(progressRunnable, 120L)
        }
    }

    fun progressTick(howMuch: Int = 10) {
//        handler = Handler(Looper.getMainLooper())
        viewModelScope.launch {
//            handler.postDelayed(progressRunnable, 120L)
            progressBar.value = progressBar.value!!.plus(howMuch)
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