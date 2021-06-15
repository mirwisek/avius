package com.gesture.avius2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    val label = MutableLiveData<String>()
    val handCount = MutableLiveData<Int>()

}