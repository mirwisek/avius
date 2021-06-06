package com.gesture.avius2

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.components.*
import com.google.mediapipe.components.CameraHelper.CameraFacing
import com.google.mediapipe.components.CameraHelper.OnCameraStartedListener
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.glutil.EglManager
import java.util.*

class MainActivity : AppCompatActivity() {

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private var previewFrameTexture: SurfaceTexture? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        processor!!
                .getVideoSurfaceOutput()
                .setFlipY(FLIP_FRAMES_VERTICALLY)

        PermissionHelper.checkAndRequestCameraPermissions(this)
        val packetCreator = processor!!.getPacketCreator()
        val inputSidePackets: MutableMap<String, Packet> = HashMap()
        inputSidePackets[INPUT_NUM_HANDS_SIDE_PACKET_NAME] = packetCreator.createInt32(NUM_HANDS)
        processor!!.setInputSidePackets(inputSidePackets)

        // To show verbose logging, run:
        // adb shell setprop log.tag.MainActivity VERBOSE

        // To show verbose logging, run:
        // adb shell setprop log.tag.MainActivity VERBOSE
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            processor!!.addPacketCallback(
                    OUTPUT_LANDMARKS_STREAM_NAME
            ) { packet: Packet ->
                Log.v(TAG, "Received multi-hand landmarks packet.")
                val multiHandLandmarks = PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser())
                for (l in multiHandLandmarks) {
                    Log.i("ffnet", "New row")
                    val list = l.landmarkList
                    val thumbTop = list[4].y
                    val littleTop = list[20].y
                    if (thumbTop < littleTop) {
                        Log.i("ffnet", "Thumb up [$thumbTop, $littleTop]")
                    } else if (list[4].y > list[20].y) {
                        Log.i("ffnet", "Thumb down [$thumbTop, $littleTop]")
                    }
                    //                            for(int i = 0; i < list.size(); i++) {
//                                NormalizedLandmark land = list.get(i);
//                                Log.i("ffnet", "" + i + " = [" + land.getX() + "," + land.getY() + "," + land.getZ() + "] ");
//
//                            }
//                            for(NormalizedLandmark j: l.getLandmarkList()) {
//                                Log.i("ffnet", "j = [" + j.getX() + "," + j.getY() + "," + j.getZ() + "] ");
//                            }
                }
                Log.v(
                        TAG,
                        "[TS:"
                                + packet.timestamp
                                + "] "
                                + getMultiHandLandmarksDebugString(multiHandLandmarks))
            }
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

    fun startCamera() {
        cameraHelper = CameraXPreviewHelper()
        cameraHelper!!.setOnCameraStartedListener(
                OnCameraStartedListener { surfaceTexture: SurfaceTexture? -> onCameraStarted(surfaceTexture!!) })
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
                .getHolder()
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

    private fun getMultiHandLandmarksDebugString(multiHandLandmarks: List<NormalizedLandmarkList>): String? {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks"
        }
        var multiHandLandmarksStr = """
                Number of hands detected: ${multiHandLandmarks.size}
                
                """.trimIndent()
        var handIndex = 0
        for (landmarks in multiHandLandmarks) {
            multiHandLandmarksStr += """	#Hand landmarks for hand[$handIndex]: ${landmarks.landmarkCount}
"""
            var landmarkIndex = 0
            for (landmark in landmarks.landmarkList) {
                multiHandLandmarksStr += """		Landmark [$landmarkIndex]: (${landmark.x}, ${landmark.y}, ${landmark.z})
"""
                ++landmarkIndex
            }
            ++handIndex
        }
        return multiHandLandmarksStr
    }

    companion object {
        init {
            // Load all native libraries needed by the app.
            System.loadLibrary("mediapipe_jni")
            System.loadLibrary("opencv_java3")
        }

        private val TAG = "MainActivity"
        private val BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb"
        private val INPUT_VIDEO_STREAM_NAME = "input_video"
        private val OUTPUT_VIDEO_STREAM_NAME = "output_video"
        private val OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks"
        private val INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands"
        private val NUM_HANDS = 2
        private val CAMERA_FACING = CameraFacing.FRONT
        // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
        // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
        // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
        // corner, whereas MediaPipe in general assumes the image origin is at top-left.
        private const val FLIP_FRAMES_VERTICALLY = true
    }
}