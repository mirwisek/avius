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
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.customui.CustomDialog
import com.gesture.avius2.customui.GestureButton
import com.gesture.avius2.customui.QuestionsPagerAdapter
import com.gesture.avius2.model.Question
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.toast
import com.gesture.avius2.viewmodels.QuestionViewModel
import com.gesture.avius2.viewmodels.StartViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.mediapipe.formats.proto.LandmarkProto

class QuestionsFragment : Fragment() , OnPacketListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var quesProgressBar: ProgressBar
    private lateinit var tvThumbUp: TextView
    private lateinit var tvThumbDown: TextView

    private lateinit var vmQuestions: QuestionViewModel
    private var countDownTimer: CountDownTimer? = null
    private lateinit var handler: Handler
    private val resetRunnable = {
        resetCounter()
    }

    private var shouldTakeInput = false

    companion object {
        const val TAG = "Avius.QuestionFragment"
        const val TIMER_COUNT = 3000L
        const val TICK = 100L
        const val NEXT_QUESTION_DELAY = 5000L
        const val PROGRESS_APPROVAL_THRESHOLD = 28  // Max Progressbar value is 30
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
        tvThumbUp = v.findViewById(R.id.thumbUpLabel)
        tvThumbDown = v.findViewById(R.id.thumbDownLabel)
        val questionStat = v.findViewById<TextView>(R.id.textQuestionStat)

        val fragments = arrayListOf<QuestionHolderFragment>()
        vmQuestions.questions.observe(viewLifecycleOwner) {
            it?.let { list ->
                list.forEach { item ->
                    fragments.add(QuestionHolderFragment.newInstance(item.question))
                }
                if(list.size > 0)
                    vmQuestions.currentQuestion.postValue(list[0])
                vmQuestions.questions.removeObservers(viewLifecycleOwner)
            }
        }
        quesProgressBar.max = fragments.size

        vmQuestions.currentQuestion.observe(viewLifecycleOwner) {
            it?.let { question ->
                questionStat.text = "${question.index}/${fragments.size}"
                updateQuestionProgress(question.index)
                tvThumbUp.text = question.upAnswers
                tvThumbDown.text = question.downAnswers
            }
        }

        val adapter = QuestionsPagerAdapter(lifecycle, fragments, childFragmentManager)
        viewPager.adapter = adapter

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler(Looper.getMainLooper())

        // Start Listening to packets
        (requireActivity() as MainActivity).setPacketListener(this, TAG)

        // Enable input after delay, give user a chance to read the question
        handler.postDelayed({
            shouldTakeInput = true
        }, NEXT_QUESTION_DELAY)

        /**
         * Observe Model values
         */
        vmQuestions.thumbStatus.observe(viewLifecycleOwner) {
            // Activate on thumbup or thumbdown
            if (it == 1 || it == -1) {
                if (countDownTimer == null) {
                    countDownTimer = getCountDownTimer().start()
                }
            }
        }

        val btnThumbsUp = view.findViewById<GestureButton>(R.id.btnThumbsUp)
        vmQuestions.progressBarUp.observe(viewLifecycleOwner) {
            btnThumbsUp.progressBar.progress = it
        }

        val btnThumbsDown = view.findViewById<GestureButton>(R.id.btnThumbsDown)
        vmQuestions.progressBarDown.observe(viewLifecycleOwner) {
            btnThumbsDown.progressBar.progress = it
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
        val dir = vmQuestions.thumbStatus.value!!
        val progressBar = if(dir == 1) vmQuestions.progressBarUp else vmQuestions.progressBarDown
        // After the threshold progress is reached take the input as accepted
        if(progressBar.value!! < PROGRESS_APPROVAL_THRESHOLD) {
            // 0 means no thumb detected
            if (vmQuestions.thumbStatus.value == 0) {
                resetCounter()
            } else {
                val lastTime = vmQuestions.handDetectedLastTimestamp.value
                // When the last frame that detected hand, was twice the tick rate then reset
                // When it is single TICK then it undos TOO OFTEN
                if (lastTime != null && System.currentTimeMillis().minus(lastTime) > TICK * 2)
                    resetCounter()
                else // Proceed
                    vmQuestions.tick()
            }
        } else {
            saveAnswer(dir)
            nextQuestion()
            resetCounter()
        }
    }

    private fun saveAnswer(direction: Int) {
        val q = vmQuestions.currentQuestion.value!!
        val ans = if(direction == 1) q.upAnswers else q.downAnswers
        vmQuestions.currentQuestion.value!!.answer = ans
    }

    private fun nextQuestion() {
        shouldTakeInput = false
        // TODO: If app crashes then adjust back in if-else clause
        val next = viewPager.currentItem + 1
        if(next <= viewPager.size) {
            vmQuestions.nextQuestion()
            viewPager.setCurrentItem(next, true)
            handler.postDelayed({
                shouldTakeInput = true
            }, NEXT_QUESTION_DELAY)
        } else {
            exitApp()
        }
    }

    private fun resetCounter() {
        countDownTimer?.cancel()
        countDownTimer = null
        vmQuestions.progressBarUp.value = 0
        vmQuestions.progressBarDown.value = 0
    }

    private fun exitApp() {
        requireActivity().finish()
    }

    override fun onLandmarkPacket(direction: Int) {
        if(shouldTakeInput) {
            vmQuestions.thumbStatus.postValue(direction)
        }
    }

    override fun onHandednessPacket(handedness: List<LandmarkProto.Landmark>) {
        if(shouldTakeInput) {
            vmQuestions.handDetectedLastTimestamp.postValue(System.currentTimeMillis())
            // Will be delaying resetRunnable while the progress completes
            // otherwise if hand leaves the frame then resetRunnable will be called
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed(resetRunnable, 1000L)
        }
    }
}