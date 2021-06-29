package com.gesture.avius2.ui

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.viewmodels.AppViewModel

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val vmApp = ViewModelProvider(this).get(AppViewModel::class.java)

        var intent = Intent(this, StartActivity::class.java)
        // Database is pre-filled then no need to fetch data from API
        vmApp.getDbQuestions().observe(this) { questions ->
            questions?.apply {
                if(isNotEmpty()) {
                    // TODO: Change to MainActivity
                    intent = Intent(this@SplashActivity, TestActivity::class.java)
                    (application as App).repository.questions = this
                }
            }
        }

        val animation = findViewById<LottieAnimationView>(R.id.animation)
        animation.playAnimation()

        animation.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                startActivity(intent)
                finish()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })
    }
}