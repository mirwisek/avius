package com.gesture.avius2.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.customui.GestureButton
import com.gesture.avius2.customui.QuestionsPagerAdapter
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.updateAutoAnimate
import com.gesture.avius2.viewmodels.QuestionViewModel
import com.google.mediapipe.formats.proto.LandmarkProto

class QuestionsFragment : Fragment() , OnPacketListener {

    private var surveyCompleteListener: OnSurveyCompleteListener? = null
    private var mainActivity: MainActivity? = null
    private lateinit var activityDebug: TestActivity

    private lateinit var viewPager: ViewPager2
    private lateinit var quesProgressBar: ProgressBar
    private lateinit var progressTimeout: ProgressBar
    private lateinit var tvThumbUp: TextView
    private lateinit var tvThumbDown: TextView

    private lateinit var vmQuestions: QuestionViewModel
    private var countDownTimer: CountDownTimer? = null
    private lateinit var handler: Handler
    private lateinit var handlerTimeout: Handler
    private val resetRunnable = {
        resetCounter()
    }

    private val timeoutRunnable = {
        updateTimeoutProgress()
    }

    private var shouldTakeInput = false
    private lateinit var adapter: QuestionsPagerAdapter
    private var themeColor: Int = -1

    companion object {
        const val TAG = "Avius.QuestionFragment"
        const val TIMER_COUNT = 3000L
        const val TICK = 100L
        const val NEXT_QUESTION_DELAY = 3000L
        const val INACTIVITY_TIMEOUT = 30_000L
        // Value after which the thumb status is taken as selected
        const val PROGRESS_APPROVAL_THRESHOLD = 28  // Max Progressbar value is 30
    }

    override fun onResume() {
        val qStartedAt = vmQuestions.qStartTime.value
        val now = System.currentTimeMillis()

        when {
            //
            qStartedAt == null -> {
                vmQuestions.qStartTime.postValue(now)
            }
            // If 40 seconds past and no response from user, RESET survey
            (now - qStartedAt) > INACTIVITY_TIMEOUT -> {
                surveyCompleteListener?.onTimeout()
                return
            }
        }
        super.onResume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            surveyCompleteListener = context as OnSurveyCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnSurveyCompleteListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        surveyCompleteListener = null
    }

    // Make sure there are no pending callbacks, on Exit
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        handlerTimeout.removeCallbacksAndMessages(null)
        // Safely Remove callbacks
        mainActivity?.removePacketListener(TAG)
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

        progressTimeout = v.findViewById<ProgressBar>(R.id.progressTimeout).apply {
            max = INACTIVITY_TIMEOUT.toInt()
        }

        themeColor = (requireActivity().application as App).themeColor

        viewPager = v.findViewById(R.id.vpQuestion)
        viewPager.isUserInputEnabled = false

        quesProgressBar = v.findViewById(R.id.progress)
        tvThumbUp = v.findViewById<TextView>(R.id.thumbUpLabel).apply {
            setTextColor(themeColor)
        }
        tvThumbDown = v.findViewById<TextView>(R.id.thumbDownLabel).apply {
            setTextColor(themeColor)
        }
        val questionStat = v.findViewById<TextView>(R.id.textQuestionStat)

        val fragments = arrayListOf<QuestionHolderFragment>()
        adapter = QuestionsPagerAdapter(lifecycle, childFragmentManager)
        viewPager.adapter = adapter

        /**
         * Customize Questions ProgressBar thickness & margins
         * innerRadiusRatio: Lower value keeps the progress away from center
         * thicknessRatio: Lower value means fat line
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val progressDrawable = quesProgressBar.progressDrawable as LayerDrawable
            val dProgress = progressDrawable.findDrawableByLayerId(android.R.id.progress) as GradientDrawable
            val dSecondary = progressDrawable.findDrawableByLayerId(android.R.id.secondaryProgress) as GradientDrawable
            val ratios = Pair(20F, 2.2F)
            dProgress.thicknessRatio = ratios.first
            dProgress.innerRadiusRatio = ratios.second

            dSecondary.thicknessRatio = ratios.first
            dSecondary.innerRadiusRatio = ratios.second
            quesProgressBar.invalidate()
        }


        vmQuestions.questions.observe(viewLifecycleOwner) {
            it?.let { list ->
                list.forEach { item ->
                    fragments.add(QuestionHolderFragment.newInstance(item.question, themeColor))
                }
                if(list.size > 0)
                    vmQuestions.currentQuestion.postValue(list[0])

                adapter.setList(fragments)
                quesProgressBar.max = fragments.size
                vmQuestions.questions.removeObservers(viewLifecycleOwner)
            }
        }

        vmQuestions.currentQuestion.observe(viewLifecycleOwner) {
            it?.let { question ->
                questionStat.text = "${question.index}/${fragments.size}"
                updateQuestionProgress(question.index)
                tvThumbUp.text = question.upAnswers
                tvThumbDown.text = question.downAnswers
            }
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        handlerTimeout = Handler(Looper.getMainLooper())

        requireActivity().apply {
            if(this is MainActivity) {
                // Start Listening to packets
                mainActivity = this
                mainActivity!!.setPacketListener(this@QuestionsFragment, TAG)
            } else if(this is TestActivity)
                activityDebug = this
        }

        vmQuestions.progressTimeout.observe(viewLifecycleOwner) {
            if(it < 3000L) {
                // Reset
                surveyCompleteListener?.onTimeout()
            }
            progressTimeout.progress = it
        }

        // Enable input after delay, give user a chance to read the question
        handler.postDelayed({
            shouldTakeInput = true
            // Now since input is enable start the progressbar
            timeoutRunnable()
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
        btnThumbsUp.setOnClickListener {
            proceedNext(1)
        }

        val btnThumbsDown = view.findViewById<GestureButton>(R.id.btnThumbsDown)
        vmQuestions.progressBarDown.observe(viewLifecycleOwner) {
            btnThumbsDown.progressBar.progress = it
        }
        btnThumbsDown.setOnClickListener {
            proceedNext(-1)
        }
    }

    private fun updateTimeoutProgress() {
        vmQuestions.qStartTime.value?.let {  old ->
            val now = System.currentTimeMillis()
            val diff = now - old
            val progress = INACTIVITY_TIMEOUT - diff
            vmQuestions.progressTimeout.postValue(progress.toInt())
            // Reschedule
            handlerTimeout.postDelayed(timeoutRunnable, 100L)
        }
    }

    private fun updateQuestionProgress(value: Int) {
        quesProgressBar.updateAutoAnimate(value)
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
            proceedNext(dir)
        }
    }

    /**
     * @param directionThumb 1 means ThumbUp was selected while -1 means ThumbDown
     */
    private fun proceedNext(directionThumb: Int) {
        saveAnswer(directionThumb)
        nextQuestion()
        resetCounter()
    }

    private fun saveAnswer(direction: Int) {
        val q = vmQuestions.currentQuestion.value!!
        val ans = if(direction == 1) q.upAnswers else q.downAnswers
        vmQuestions.saveAnswer(q.index - 1, ans)
    }

    private fun nextQuestion() {
        playSound()
        shouldTakeInput = false
        val next = viewPager.currentItem + 1
        if(next < adapter.itemCount) {
            // Reset timeout progress
            vmQuestions.qStartTime.postValue(System.currentTimeMillis())
            vmQuestions.progressTimeout.postValue(INACTIVITY_TIMEOUT.toInt())

            vmQuestions.nextQuestion()
            viewPager.setCurrentItem(next, true)
            handler.postDelayed({
                shouldTakeInput = true
            }, NEXT_QUESTION_DELAY)
        } else {
            vmQuestions.storeAnswers()  // Save ViewModel answers into App's Repository Cache
            surveyCompleteListener?.onSurveyCompleted(themeColor)
        }
    }

    private fun resetCounter() {
        countDownTimer?.cancel()
        countDownTimer = null
        vmQuestions.progressBarUp.value = 0
        vmQuestions.progressBarDown.value = 0
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

    private fun playSound(@RawRes resId: Int = R.raw.definite) {
        if(mainActivity == null) {
            activityDebug.playSound(resId)
        } else {
            mainActivity!!.playSound(resId)
        }
    }

    interface OnSurveyCompleteListener {
        fun onSurveyCompleted(themeColor: Int)
        fun onTimeout()
    }
}