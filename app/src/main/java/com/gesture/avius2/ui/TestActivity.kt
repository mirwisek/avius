package com.gesture.avius2.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.R
import com.gesture.avius2.utils.findFragmentOrInit
import com.gesture.avius2.utils.initReplace
import com.gesture.avius2.utils.replace
import com.gesture.avius2.viewmodels.MainViewModel
import com.gesture.avius2.viewmodels.StartViewModel


class TestActivity : AppCompatActivity(),
    StartFragment.OnThumbDetectionFinishListener,
    QuestionsFragment.OnSurveyCompleteListener,
    SubscriptionFragment.OnCountDownCompleteListener {

    private lateinit var vmMain: MainViewModel
    private lateinit var fragmentStart: StartFragment
    private lateinit var fragmentQuestions: QuestionsFragment
    private lateinit var fragmentSubscription: SubscriptionFragment


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

    private fun setUpStartFragment() {
        supportFragmentManager.initReplace(StartFragment.TAG) { StartFragment() }
    }

    private fun setUpQuestionsFragment() {
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