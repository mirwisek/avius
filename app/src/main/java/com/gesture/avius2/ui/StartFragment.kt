package com.gesture.avius2.ui

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.customui.CustomDialog
import com.gesture.avius2.customui.GestureButton
import com.gesture.avius2.customui.LoginDialog
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.makeNetworkSnack
import com.gesture.avius2.viewmodels.AppViewModel
import com.gesture.avius2.viewmodels.StartViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.mediapipe.formats.proto.LandmarkProto

/**
 * Don't confuse with StartActivity, StartFragment is for starting the thumb detection and
 * first step for moving towards question survey
 */
class StartFragment : Fragment(), OnPacketListener {

    private var thumbFinishListener: OnThumbDetectionFinishListener? = null

    private lateinit var vmApp: AppViewModel
    private lateinit var vmStart: StartViewModel
    private var countDownTimer: CountDownTimer? = null
    private lateinit var handler: Handler
    private val resetRunnable = {
        resetCounter()
    }

    // A variable to disable input while a dialog is in focus and waiting client input
    private var shouldTakeInput = false
    private var themeColor: Int = -1

    companion object {
        const val TAG = "Avius.StartFragment"
        const val TIMER_COUNT = 3000L
        const val TICK = 100L
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            thumbFinishListener = context as OnThumbDetectionFinishListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnThumbDetectionFinishListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        thumbFinishListener = null
    }

    // Make sure there are no pending callbacks, on Exit
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        requireActivity().apply {
            if(this is MainActivity) {
                // Start Listening to packets
                this.removePacketListener(TAG)
            }
        }
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


        val app = requireActivity().application as App
        handler = Handler(Looper.getMainLooper())
        vmStart = ViewModelProvider(this).get(StartViewModel::class.java)
        themeColor = app.themeColor

        vmApp = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)

        vmApp.isOnline.observe(viewLifecycleOwner) { isOnline ->
            if(!isOnline){  // When offline show snack red
                view.makeNetworkSnack(isOnline)
            } else if(isOnline && !app.wasOnline) {
                // When offline and switched to back online show green
                view.makeNetworkSnack(isOnline)
            }
            app.wasOnline = isOnline
        }

        // Don't initialize in tablet testing mode
        requireActivity().apply {
            if(this is MainActivity) {
                // Start Listening to packets
                this.setPacketListener(this@StartFragment, TAG)
            }
        }
        /**
         * Setup Logout Dialog
         */
        val dialogLogout = CustomDialog(requireContext()) { parent, dialog ->
            val v = layoutInflater.inflate(R.layout.layout_dialog_logout, parent)

            v.findViewById<MaterialButton>(R.id.btn_yes).setOnClickListener {

                vmApp.isOnline.value?.let { isOnline ->
                    if(isOnline) {
                        dialog.dismiss()
                        showLogin()
                    } else {
                        view.makeNetworkSnack(false)
                    }
                }
            }
            v.findViewById<MaterialButton>(R.id.btn_no).setOnClickListener {
                dialog.dismiss()
                shouldTakeInput = true
            }
        }

        view.findViewById<ExtendedFloatingActionButton>(R.id.fabPower).setOnClickListener {
            shouldTakeInput = false
            dialogLogout.show()
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

        val gestureButton = view.findViewById<GestureButton>(R.id.gestureButton).apply {
            changeCircleColor(themeColor)
            setOnClickListener {
                thumbFinishListener?.onStartFragmentFinished()
            }
        }
        val progressBar = gestureButton.progressBar

        vmStart.progressBar.observe(viewLifecycleOwner) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(it, true)
            } else {
                progressBar.progress = it
            }
        }

        // Start taking input
        shouldTakeInput = true
    }

    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(TIMER_COUNT, TICK) {
            override fun onTick(millisUntilFinished: Long) {
                if(shouldTakeInput)
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
        if(vmStart.isCancelable.value!!) {
            // 0 means no thumb detected
            if (vmStart.thumbStatus.value == 0) {
                resetCounter()
            } else if (vmStart.hasValueChanged.value == true) {
                resetCounter()
                vmStart.hasValueChanged.value = false
            } else {
                val lastTime = vmStart.handDetectedLastTimestamp.value
                // When the last frame that detected hand, was twice the tick rate then reset
                // When it is single TICK then it undos TOO OFTEN
                if (lastTime != null && System.currentTimeMillis().minus(lastTime) > TICK * 2)
                    resetCounter()
                else // Proceed
                    vmStart.tick()
            }
        } else {
            countDownTimer?.cancel()
            countDownTimer = null
            thumbFinishListener?.onStartFragmentFinished()
        }
    }

    private fun resetCounter() {
        countDownTimer?.cancel()
        countDownTimer = null
        vmStart.progressBar.value = 0
    }

    private fun showLogin() {
        val loginDialog = LoginDialog().apply {
            setOnLoginError {
                // Do nothing
            }
            setOnLoginSuccess { errorMsg ->
                // Proceed if there are no error messages
                if(errorMsg == null) {
                    // It is going to logout no need to set shouldTakeInput
                    vmStart.deleteData { logout() }
                }
            }
            // When user clicks on side to dismiss, we need to restore shouldTakeInput
            setOnDismiss { isScheduledLogout ->
                if(!isScheduledLogout) {
                    shouldTakeInput = true
                }
            }
        }
        shouldTakeInput = false
        loginDialog.showNow(childFragmentManager, LoginDialog.TAG)
    }

    private fun logout() {
        requireActivity().apply {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
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

    interface OnThumbDetectionFinishListener {
        fun onStartFragmentFinished()
    }
}