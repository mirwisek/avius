package com.gesture.avius2.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.R
import com.gesture.avius2.viewmodels.MainViewModel
import com.gesture.avius2.viewmodels.StartViewModel


class TestActivity : AppCompatActivity() {

    private lateinit var vmMain: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val vmStart = ViewModelProvider(this).get(StartViewModel::class.java)
        val themeColor = if (vmStart.themeColor.isNotBlank())
            Color.parseColor(vmStart.themeColor)
        else
            ContextCompat.getColor(this, R.color.blue_main)
        window.statusBarColor = themeColor

        val fragmentStart = (supportFragmentManager.findFragmentByTag(StartFragment.TAG)
            ?: StartFragment()) as StartFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmentStart, StartFragment.TAG)
            .commit()

        val fragmentQuestion = QuestionsFragment()

        fragmentStart.setOnFinish {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentQuestion, QuestionsFragment.TAG)
                .commit()
        }

        fragmentQuestion.setOnSurveyComplete { themeColor ->
            val fragmentSubscription = SubscriptionFragment.newInstance(themeColor)
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    fragmentSubscription,
                    SubscriptionFragment.TAG
                )
                .commit()
        }

    }

    fun onSurveyComplete() {

    }

}