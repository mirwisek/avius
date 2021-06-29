package com.gesture.avius2

import android.app.Application
import android.graphics.Color
import com.gesture.avius2.network.Repository

class App: Application() {

    val repository = Repository()
    var themeColor: Int = -1

}