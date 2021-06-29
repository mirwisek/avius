package com.gesture.avius2.ui

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.R
import com.gesture.avius2.utils.findFragmentOrInit
import com.gesture.avius2.utils.initReplace
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.replace
import com.gesture.avius2.viewmodels.MainViewModel
import com.gesture.avius2.viewmodels.StartViewModel
import kotlin.math.absoluteValue


class TestActivity : AppCompatActivity(),
    StartFragment.OnThumbDetectionFinishListener,
    QuestionsFragment.OnSurveyCompleteListener,
    SubscriptionFragment.OnCountDownCompleteListener {

    private lateinit var vmMain: MainViewModel
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val vmStart = ViewModelProvider(this).get(StartViewModel::class.java)
        val themeColor = if (vmStart.themeColor.isNotBlank())
            Color.parseColor(vmStart.themeColor)
        else
            ContextCompat.getColor(this, R.color.blue_main)
        window.statusBarColor = themeColor

        setUpStartFragment()
    }

    fun playSound(@RawRes resId: Int) {
        mediaPlayer?.let {
            if(it.isPlaying) {
                it.stop()
                it.release()
            }
        }
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            setVolume(1F, 1F)   // Set sound to max
            setOnCompletionListener { mp ->
                mp.reset()
                mp.release()
                mediaPlayer = null
            }
        }
        mediaPlayer?.start()
    }

    private fun setUpStartFragment() {
        supportFragmentManager.initReplace(StartFragment.TAG) { StartFragment() }
    }

    private fun setUpQuestionsFragment() {
        playSound(R.raw.accomplished)
        supportFragmentManager.initReplace(QuestionsFragment.TAG) { QuestionsFragment() }
    }

    private fun setUpSubscriptionFragment(themeColor: Int) {
        supportFragmentManager.initReplace(SubscriptionFragment.TAG) {
            SubscriptionFragment.newInstance(themeColor)
        }
    }

    override fun onStartFragmentFinished() {
        setUpQuestionsFragment()
    }

    override fun onSurveyCompleted(themeColor: Int) {
        setUpSubscriptionFragment(themeColor)
    }

    override fun onCountDownCompleted() {
        setUpStartFragment()
    }

}