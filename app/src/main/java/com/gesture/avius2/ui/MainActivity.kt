package com.gesture.avius2.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.BuildConfig
import com.gesture.avius2.R
import com.gesture.avius2.customui.CustomDialog
import com.gesture.avius2.utils.gone
import com.gesture.avius2.viewmodels.MainViewModel
import com.gesture.avius2.viewmodels.StartViewModel
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.components.CameraXPreviewHelper
import com.google.mediapipe.components.ExternalTextureConverter
import com.google.mediapipe.components.FrameProcessor
import com.google.mediapipe.components.PermissionHelper
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager
import java.util.*


class MainActivity : AppCompatActivity() {

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private var previewFrameTexture: SurfaceTexture? = null
    private lateinit var labelText: TextView
    private lateinit var labelPoints: TextView

    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private var previewDisplayView: SurfaceView? = null

    // Creates and manages an {@link EGLContext}.
    private var eglManager: EglManager? = null

    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private var processor: FrameProcessor? = null

    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private var converter: ExternalTextureConverter? = null

    // ApplicationInfo for retrieving metadata defined in the manifest.
    private var appInfo: ApplicationInfo? = null

    // Handles camera access via the {@link CameraX} Jetpack support library.
    private var cameraHelper: CameraXPreviewHelper? = null

    private val packetListeners = hashMapOf<String, OnPacketListener>()

    private lateinit var vmMain: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (wasAbleToLoadLibrary) {

            setContentView(R.layout.activity_main)

            vmMain = ViewModelProvider(this).get(MainViewModel::class.java)
            val vmStart = ViewModelProvider(this).get(StartViewModel::class.java)

            /**
             * Set theme color for the statusbar from the API
             */
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

            labelText = findViewById(R.id.label)
            labelPoints = findViewById(R.id.points)

            try {
                appInfo =
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Cannot find application info: $e")
            }

            previewDisplayView = SurfaceView(this)
            setupPreviewDisplayView()

            // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
            // binary graphs.
            AndroidAssetUtil.initializeNativeAssetManager(this)
            eglManager = EglManager(null)

            processor = FrameProcessor(
                this,
                eglManager!!.nativeContext,
                BINARY_GRAPH_NAME,
                INPUT_VIDEO_STREAM_NAME,
                OUTPUT_VIDEO_STREAM_NAME
            )

            processor!!.videoSurfaceOutput
                .setFlipY(FLIP_FRAMES_VERTICALLY)


            PermissionHelper.checkAndRequestCameraPermissions(this)
            val packetCreator = processor!!.packetCreator
            val inputSidePackets: MutableMap<String, Packet> = HashMap()
            inputSidePackets[INPUT_NUM_HANDS_SIDE_PACKET_NAME] =
                packetCreator.createInt32(NUM_HANDS)
            processor!!.setInputSidePackets(inputSidePackets)

            processor!!.addPacketCallback(OUTPUT_HANDEDNESS_STREAM_NAME) { packet ->
                val handedness =
                    PacketGetter.getProtoVector(packet, LandmarkProto.Landmark.parser())

                packetListeners.forEach { (_, v) ->
                    v.onHandednessPacket(handedness)
                }
            }

            // To show verbose logging, run:
            // adb shell setprop log.tag.MainActivity VERBOSE
            processor!!.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME
            ) { packet: Packet ->

                updateLabel("")

                val multiHandLandmarks =
                    PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser())

                for (l in multiHandLandmarks) {
                    val list = l.landmarkList
//                LandmarkProcessor.process(list, packetListeners)
                    val str = LandmarkProcessor.process2(list) { direction ->
                        packetListeners.forEach {
                            it.value.onLandmarkPacket(direction)
                        }
                    }
                    runOnUiThread {
                        labelPoints.text = str
                    }
                }

            }
        } else {
            CustomDialog(this) { parent, dialog ->
                val v = layoutInflater.inflate(R.layout.layout_dialog_loading, parent)
                v.findViewById<ProgressBar>(R.id.progress).gone()
                v.findViewById<TextView>(R.id.msg).text =
                    "Sorry! Your device doesn't support running this App"
                parent.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.setOnDismissListener {
                    finish()
                }
            }.show()
        }

    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        // Checks the orientation of the screen
//        val rotation = windowManager.defaultDisplay.rotation
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            converter!!.setRotation((360 - rotation) % 360)
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            converter!!.setRotation((360 - rotation) % 360)
//        }
//    }

    /**
     * When fragment is ready it will make a call to activity to listener for packets
     * after it has initialized all variables
     */
    fun setPacketListener(fragment: Fragment, tag: String) {
        packetListeners[tag] = fragment as OnPacketListener
    }

    fun removePacketListener(tag: String) {
        packetListeners.remove(tag)
    }

    fun onSurveyComplete() {

    }

    private fun updateLabel(text: String) {
//        runOnUiThread {
//            labelText.text = text
////            handler.removeCallbacksAndMessages(null)
////            handler.postDelayed(resetRunnable, 3000L)
//        }
    }

    override fun onResume() {
        super.onResume()
        if (wasAbleToLoadLibrary) {
            converter = ExternalTextureConverter(
                eglManager!!.context, 2
            )
            converter!!.setFlipY(FLIP_FRAMES_VERTICALLY)
            converter!!.setConsumer(processor)
            if (PermissionHelper.cameraPermissionsGranted(this)) {
                startCamera()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (wasAbleToLoadLibrary) {
            converter!!.close()
            // Hide preview display until we re-open the camera again.
            previewDisplayView!!.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    protected fun onCameraStarted(surfaceTexture: SurfaceTexture) {
        previewFrameTexture = surfaceTexture
        // Make the display view visible to start showing the preview. This triggers the
        // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
        previewDisplayView!!.visibility = View.VISIBLE
    }

    protected fun cameraTargetResolution(): Size? {
        return null // No preference and let the camera (helper) decide.
    }

    private fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            onCameraStarted(surfaceTexture!!)
        }

        val cameraFacing = CameraFacing.FRONT
        cameraHelper!!.startCamera(
            this, cameraFacing,  /*unusedSurfaceTexture=*/null, cameraTargetResolution()
        )
    }

    protected fun computeViewSize(width: Int, height: Int): Size {
        return Size(width, height)
    }

    protected fun onPreviewDisplaySurfaceChanged(
        holder: SurfaceHolder?, format: Int, width: Int, height: Int
    ) {

        // (Re-)Compute the ideal size of the camera-preview display (the area that the
        // camera-preview frames get rendered onto, potentially with scaling and rotation)
        // based on the size of the SurfaceView that contains the display.
        val viewSize = computeViewSize(width, height)
        val displaySize = cameraHelper!!.computeDisplaySizeFromViewSize(viewSize)
        val isCameraRotated = cameraHelper!!.isCameraRotated

        // Connect the converter to the camera-preview frames as its input (via
        // previewFrameTexture), and configure the output width and height as the computed
        // display size.
        converter!!.setSurfaceTextureAndAttachToGLContext(
            previewFrameTexture,
            if (isCameraRotated) displaySize.height else displaySize.width,
            if (isCameraRotated) displaySize.width else displaySize.height
        )
        /**
         *  FIX CameraBlackout ISSUE
         *  Github ISSUE: https://github.com/google/mediapipe/issues/1508
         */
        previewFrameTexture!!.updateTexImage()
    }


    private fun setupPreviewDisplayView() {
        previewDisplayView!!.visibility = View.GONE
        val viewGroup = findViewById<ViewGroup>(R.id.preview_display_layout)
        viewGroup.addView(previewDisplayView)
        previewDisplayView!!
            .holder
            .addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        onPreviewDisplaySurfaceChanged(holder, format, width, height)
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        processor!!.videoSurfaceOutput.setSurface(null)
                    }
                })
    }

    companion object {

        private var wasAbleToLoadLibrary = true

        init {
            // Load all native libraries needed by the app.
//            try {
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
//            } catch (e: UnsatisfiedLinkError) {
//                log("Error $e")
//                wasAbleToLoadLibrary = false
//            }
        }

        private const val TAG = "MainActivity"
        private const val BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb"
        private const val INPUT_VIDEO_STREAM_NAME = "input_video"
        private const val OUTPUT_VIDEO_STREAM_NAME = "output_video"
        private const val OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks"
        private const val OUTPUT_HANDEDNESS_STREAM_NAME = "handedness"
        private const val INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands"
        private const val NUM_HANDS = 2
        private val CAMERA_FACING = CameraFacing.FRONT

        // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
        // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
        // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
        // corner, whereas MediaPipe in general assumes the image origin is at top-left.
        private const val FLIP_FRAMES_VERTICALLY = true
    }
}