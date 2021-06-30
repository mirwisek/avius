package com.gesture.avius2

import android.app.Application
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.gesture.avius2.network.Repository
import com.gesture.avius2.utils.sharedPrefs

const val KEY_THEME = "theme.color"

class App: Application() {

    val repository = Repository()
    var themeColor: Int = -1

    override fun onCreate() {
        super.onCreate()

        sharedPrefs.getString(KEY_THEME, null)?.let {
            themeColor = Color.parseColor(it)
        }

        if(themeColor == -1)
            themeColor = ContextCompat.getColor(applicationContext, R.color.blue_main)

    }

}