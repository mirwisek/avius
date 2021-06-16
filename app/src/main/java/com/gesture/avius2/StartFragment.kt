package com.gesture.avius2

import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.mediapipe.formats.proto.LandmarkProto

class StartFragment : Fragment(), OnPacketListener {

    private lateinit var vmStart: StartViewModel
    private var countDownTimer: CountDownTimer? = null
    // Callback when the thumb progress completes
    private var onFinish: (() -> Unit)? = null
    private lateinit var handler: Handler
    private val resetRunnable = {
        resetCounter()
    }

    companion object {
        const val TAG = "Avius.StartFragment"
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        vmStart = ViewModelProvider(this).get(StartViewModel::class.java)
        /**
         * Setup Exit Dialog
         */
        val dialogExit = CustomDialog(requireContext()).apply {
            setOnYesListener { exitApp() }
            setOnNoListener { dismiss() }
        }

        view.findViewById<ExtendedFloatingActionButton>(R.id.fabPower).setOnClickListener {
            dialogExit.show()
        }


        /**
         * Observe Model values
         */

        vmStart.thumbStatus.observe(viewLifecycleOwner) {
            if (it == 1) {
                if (countDownTimer == null) {
                    countDownTimer = getCountDownTimer().start()
                }

            }
        }

        val progressBar = view.findViewById<ProgressBar>(R.id.progress)

        vmStart.progressBar.observe(viewLifecycleOwner) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(it, true)
            } else {
                progressBar.progress = it
            }
        }

    }

    fun setOnFinish(callback: (() -> Unit)) { onFinish = callback }

    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(TIMER_COUNT, TICK) {
            override fun onTick(millisUntilFinished: Long) {
                updateCounter()
            }

            override fun onFinish() {
                onFinish?.invoke()
            }
        }
    }

    /**
     * Total Progress = 300
     * Increment = 30
     */

    private fun updateCounter() {
        // 0 means no thumb detected
        if (vmStart.thumbStatus.value == 0) {
            resetCounter()
        } else if (vmStart.hasValueChanged.value == true) {
            resetCounter()
            vmStart.hasValueChanged.value = false
        } else {
            val lastTime = vmStart.handDetectedLastTimestamp.value
            if (lastTime != null && System.currentTimeMillis().minus(lastTime) > TICK)
                resetCounter()
            else // Proceed
                vmStart.progressTick()
        }
    }

    private fun resetCounter() {
        countDownTimer?.cancel()
        countDownTimer = null
        vmStart.progressBar.value = 0
    }

    private fun exitApp() {
        requireActivity().finish()
    }

    override fun onLandmarkPacket(direction: Int) {
        vmStart.thumbStatus.postValue(direction)
    }

    override fun onHandednessPacket(handedness: List<LandmarkProto.Landmark>) {

//        vmStart.handCount.postValue(handedness.size)
        vmStart.handDetectedLastTimestamp.postValue(System.currentTimeMillis())
        // Will be delaying resetRunnable while the progress completes
        // otherwise if hand leaves the frame then resetRunnable will be called
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(resetRunnable, 1000L)
//            log("Packet:: ${handedness.size} and ${handedness[0]}")
    }
}