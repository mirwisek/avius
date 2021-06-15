package com.gesture.avius2

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.mediapipe.components.*
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.abs
import kotlin.math.round

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

    private lateinit var vmMain: MainViewModel
    private lateinit var handler: Handler
    private val resetRunnable = {
        vmMain.label.value = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_start)


        handler = Handler(Looper.getMainLooper())
        vmMain = ViewModelProvider(this).get(MainViewModel::class.java)

        labelText = findViewById(R.id.label)
        labelPoints = findViewById(R.id.points)

        vmMain.label.observe(this) { value ->
            if(value == null) {
                updateLabel("")
            } else {
                updateLabel(value)
            }
        }

        vmMain.handCount.observe(this) { value ->
            if(value != null && value > 0) {
                updateLabel(vmMain.label.value ?: "")
            } else {
                updateLabel("")
            }
        }

        try {
            appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Cannot find application info: $e")
        }

        previewDisplayView = SurfaceView(this)
        setupPreviewDisplayView()

        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.

        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(this)
        eglManager = EglManager(null)

        processor = FrameProcessor(
                this,
                eglManager!!.nativeContext,
                BINARY_GRAPH_NAME,
                INPUT_VIDEO_STREAM_NAME,
                OUTPUT_VIDEO_STREAM_NAME)
        processor!!.videoSurfaceOutput
                .setFlipY(FLIP_FRAMES_VERTICALLY)


        PermissionHelper.checkAndRequestCameraPermissions(this)
        val packetCreator = processor!!.packetCreator
        val inputSidePackets: MutableMap<String, Packet> = HashMap()
        inputSidePackets[INPUT_NUM_HANDS_SIDE_PACKET_NAME] = packetCreator.createInt32(NUM_HANDS)
        processor!!.setInputSidePackets(inputSidePackets)
//        processor!!.addConsumer {
//            log("Consumer[${it.timestamp}] ${it.width} x ${it.height}")
//        }

//        processor!!.addPacketCallback(OUTPUT_HANDEDNESS_STREAM_NAME) { packet ->
//            val handedness = PacketGetter.getProtoVector(packet, LandmarkProto.Landmark.parser())
//            vmMain.handCount.value = handedness.size
////            log("Packet:: ${handedness.size}")
//        }

        // To show verbose logging, run:
        // adb shell setprop log.tag.MainActivity VERBOSE
        processor!!.addPacketCallback(
            OUTPUT_LANDMARKS_STREAM_NAME
        ) { packet: Packet ->
            updateLabel("")
            Log.v(TAG, "Received multi-hand landmarks packet.")
            val multiHandLandmarks = PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser())

            for (l in multiHandLandmarks) {
                val list = l.landmarkList
                printPoints(list)
//                val thumbTop = list[4].y
//                val littleTop = list[20].y
////                    val max = list.maxOfOrNull { landmark ->
////                        landmark.y
////                    }
//                val exclusiveList = list.filterIndexed { i, _ -> i != 4 }
//                val listMax = exclusiveList.maxOf { landmark -> landmark.y }
//                val listMin = exclusiveList.minOf { landmark -> landmark.y }
//                if(thumbTop < listMax) {
////                        log("Thumbs up")
//                    updateLabel("Thumbs UP")
//                } else if(thumbTop > listMin) {
////                        log("Thumbs down")
//                    updateLabel("Thumbs DOWN")
//                } else {
////                        log("No Thumbs")
//                    updateLabel("")
//                }
            }
            Log.v(
                TAG,
                "[TS:"
                        + packet.timestamp
                        + "] "
                        + getMultiHandLandmarksDebugString(multiHandLandmarks))

        }

//        if (Log.isLoggable(TAG, Log.VERBOSE)) {
//
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun printPoints(nList: List<LandmarkProto.NormalizedLandmark>) {

        val dir = GestureCompareUtils.getVerticalDirection(nList)

        val tip = nList.getPoints(GestureCompareUtils.TIP)
        val dip = nList.getPoints(GestureCompareUtils.DIP)
        val pip = nList.getPoints(GestureCompareUtils.PIP)
        val mcp = nList.getPoints(GestureCompareUtils.MCP)
        val thumbLine = nList.getPoints(GestureCompareUtils.THUMB_LINE)

        log("Tip size is ${tip.strLine(GestureCompareUtils.TIP) }")

//        val isTip = tip.areParallel()
//        val isDip = dip.areParallel()
//        val isPip = pip.areParallel()
//        val isMcp = mcp.areParallel()
        val isThumbTIP = (thumbLine + tip).areParallel(0.2F)
        val isThumbDIP = (thumbLine + dip).areParallel(0.2F)
        val isThumbPIP = (thumbLine + pip).areParallel(0.2F)
        val isThumbMCP = (thumbLine + mcp).areParallel(0.2F)

        val m = if(isThumbMCP) "mcp" else if(isThumbDIP) "dip" else if(isThumbPIP) "pip" else "tip"
        val res = if(/*isTip && isDip && isPip && isMcp*/isThumbMCP || isThumbPIP || isThumbDIP || isThumbTIP)
            "THUMB $dir" /*+ "($m)"*/
        else
            "NO THUMB"

        runOnUiThread {
            labelPoints.text = res + "\n"

//                tip.strLine(GestureCompareUtils.TIP) + "\n\n" +
//                dip.strLine(GestureCompareUtils.DIP) + "\n\n" +
//                pip.strLine(GestureCompareUtils.PIP) + "\n\n" +
//                mcp.strLine(GestureCompareUtils.MCP)
        }
    }

    private fun updateLabel(text: String) {
        runOnUiThread {
            labelText.text = text
//            handler.removeCallbacksAndMessages(null)
//            handler.postDelayed(resetRunnable, 3000L)
        }
    }

    override fun onResume() {
        super.onResume()
        converter = ExternalTextureConverter(
                eglManager!!.context, 2)
        converter!!.setFlipY(FLIP_FRAMES_VERTICALLY)
        converter!!.setConsumer(processor)
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        converter!!.close()
        // Hide preview display until we re-open the camera again.
        previewDisplayView!!.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                this, cameraFacing,  /*unusedSurfaceTexture=*/null, cameraTargetResolution())
    }

    protected fun computeViewSize(width: Int, height: Int): Size {
        return Size(width, height)
    }

    protected fun onPreviewDisplaySurfaceChanged(
            holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
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
                if (isCameraRotated) displaySize.width else displaySize.height)
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

                            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height)
                            }

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                processor!!.videoSurfaceOutput.setSurface(null)
                            }
                        })
    }

    companion object {
        init {
            // Load all native libraries needed by the app.
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
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