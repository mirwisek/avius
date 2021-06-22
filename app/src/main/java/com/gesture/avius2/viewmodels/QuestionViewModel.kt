package com.gesture.avius2.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class QuestionViewModel: ViewModel() {

    val thumbStatus = MutableLiveData(0)
    val progressBarUp = MutableLiveData(0)
    val progressBarDown = MutableLiveData(0)

    val currentQuestion = MutableLiveData(0)
    val totalQuestions = MutableLiveData(0)

    val handDetectedLastTimestamp = MutableLiveData<Long>()

    fun nextQuestion() {
        if(currentQuestion.value!! <= totalQuestions.value!!)
            currentQuestion.postValue(currentQuestion.value!! + 1)
    }

    fun tick() {
        viewModelScope.launch {
            if(thumbStatus.value == 1) {
                progressBarUp.value = progressBarUp.value!!.plus(3)
                progressBarDown.value = 0   // Reset the other
            } else if(thumbStatus.value == -1) {
                progressBarDown.value = progressBarDown.value!!.plus(3)
                progressBarUp.value = 0   // Reset the other
            }
        }
    }

    fun resetViewModel() {
        thumbStatus.postValue(0)
        progressBarDown.postValue(0)
        progressBarUp.postValue(0)
        handDetectedLastTimestamp.postValue(0)
    }

}