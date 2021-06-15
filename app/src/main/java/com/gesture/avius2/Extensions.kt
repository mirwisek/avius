package com.gesture.avius2

import android.util.Log
import com.google.mediapipe.formats.proto.LandmarkProto
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

fun log(msg: String, tag: String = "ffnet") {
    Log.i(tag, msg)
}

fun getMultiHandLandmarksDebugString(multiHandLandmarks: List<LandmarkProto.NormalizedLandmarkList>): String {
    if (multiHandLandmarks.isEmpty()) {
        return "No hand landmarks"
    }
    var multiHandLandmarksStr = """
                Number of hands detected: ${multiHandLandmarks.size}
                
                """.trimIndent()
    for ((handIndex, landmarks) in multiHandLandmarks.withIndex()) {
        multiHandLandmarksStr += """	#Hand landmarks for hand[$handIndex]: ${landmarks.landmarkCount}
                                     """
        for ((landmarkIndex, landmark) in landmarks.landmarkList.withIndex()) {
            multiHandLandmarksStr += """		Landmark [$landmarkIndex]: (${landmark.x}, ${landmark.y}, ${landmark.z})
                                         """
        }
    }
    return multiHandLandmarksStr
}

fun Double.roundTo(decimals: Int, roundingMode: RoundingMode = RoundingMode.CEILING): Double {
    return BigDecimal(this).setScale(decimals, roundingMode).toDouble()
}

fun Float.roundTo(decimals: Int, roundingMode: RoundingMode = RoundingMode.CEILING): Float {
    return toDouble().roundTo(decimals, roundingMode).toFloat()
}