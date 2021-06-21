package com.gesture.avius2.ui

import android.annotation.SuppressLint
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.gesture.avius2.R
import com.gesture.avius2.customui.CustomDialog
import com.gesture.avius2.customui.QuestionsPagerAdapter
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.toast
import com.gesture.avius2.viewmodels.QuestionViewModel
import com.gesture.avius2.viewmodels.StartViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.mediapipe.formats.proto.LandmarkProto

class QuestionsFragment : Fragment() , OnPacketListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var quesProgressBar: ProgressBar

    private lateinit var vmQuestions: QuestionViewModel
    private var countDownTimer: CountDownTimer? = null
    private lateinit var handler: Handler
    private val resetRunnable = {
        resetCounter()
    }

    companion object {
        const val TAG = "Avius.QuestionFragment"
        const val TIMER_COUNT = 3000L
        const val TICK = 100L
    }


    // Make sure there are no pending callbacks, on Exit
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        // Safely Remove callbacks
        (requireActivity() as MainActivity).removePacketListener(TAG)
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_question, container, false)
        vmQuestions = ViewModelProvider(this).get(QuestionViewModel::class.java)

        viewPager = v.findViewById(R.id.vpQuestion)
        quesProgressBar = v.findViewById(R.id.progress)
        val questionStat = v.findViewById<TextView>(R.id.textQuestionStat)

        val fragments = listOf(
            QuestionHolderFragment.newInstance("How was your day?"),
            QuestionHolderFragment.newInstance("How was your stay?")
        )
        quesProgressBar.max = fragments.size
        vmQuestions.currentQuestion.observe(viewLifecycleOwner) {
            questionStat.text = "$it/${fragments.size}"
        }

        val adapter = QuestionsPagerAdapter(lifecycle, fragments, childFragmentManager)
        viewPager.adapter = adapter

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        vmQuestions.currentQuestion.postValue(1)

        // Start Listening to packets
        (requireActivity() as MainActivity).setPacketListener(this, TAG)

        /**
         * Observe Model values
         */

        vmQuestions.thumbStatus.observe(viewLifecycleOwner) {
            if (it == 1) {
                if (countDownTimer == null) {
                    countDownTimer = getCountDownTimer().start()
                }
            }
        }

        vmQuestions.currentQuestion.observe(viewLifecycleOwner) {
            updateQuestionProgress(it)
        }
    }

    // Reset values in ViewModel for new question
    private fun resetViewModel() {

    }

    private fun updateQuestionProgress(value: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            quesProgressBar.setProgress(value, true)
        } else {
            quesProgressBar.progress = value
        }
    }

    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(TIMER_COUNT, TICK) {
            override fun onTick(millisUntilFinished: Long) {
                updateCounter()
            }

            override fun onFinish() { }
        }
    }

    /**
     * Total Progress = 300
     * Increment = 30
     */

    private fun updateCounter() {
        // While it is cancelable then make checks otherwise continue
        if(vmQuestions.isCancelable.value!!) {
            // 0 means no thumb detected
            if (vmQuestions.thumbStatus.value == 0) {
                resetCounter()
            } else if (vmQuestions.hasValueChanged.value == true) {
                resetCounter()
                vmQuestions.hasValueChanged.value = false
            } else {
                val lastTime = vmQuestions.handDetectedLastTimestamp.value
                if (lastTime != null && System.currentTimeMillis().minus(lastTime) > StartFragment.TICK)
                    resetCounter()
                else // Proceed
//                vmStart.continueProgress()
                    vmQuestions.tick()
            }
        } else {
            nextQuestion()
            resetCounter()
        }
    }

    private fun nextQuestion() {
        val next = viewPager.currentItem + 1
        if(next < viewPager.size) {
            viewPager.setCurrentItem(next, true)
        }
    }

    private fun resetCounter() {
        countDownTimer?.cancel()
        countDownTimer = null
        vmQuestions.progressBar.value = 0
    }

    private fun exitApp() {
        requireActivity().finish()
    }

    override fun onLandmarkPacket(direction: Int) {
        vmQuestions.thumbStatus.postValue(direction)
    }

    override fun onHandednessPacket(handedness: List<LandmarkProto.Landmark>) {

//        vmStart.handCount.postValue(handedness.size)
        vmQuestions.handDetectedLastTimestamp.postValue(System.currentTimeMillis())
        // Will be delaying resetRunnable while the progress completes
        // otherwise if hand leaves the frame then resetRunnable will be called
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(resetRunnable, 1000L)
//            log("Packet:: ${handedness.size} and ${handedness[0]}")
    }
}