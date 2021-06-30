package com.gesture.avius2.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.gesture.avius2.R
import com.google.mediapipe.formats.proto.LandmarkProto
import java.math.BigDecimal
import java.math.RoundingMode

val Context.sharedPrefs: SharedPreferences
    get() {
        return getSharedPreferences("avius.touchless.ui.shared-prefs", Context.MODE_PRIVATE)
    }

fun FragmentManager.replace(fragment: Fragment, tag: String, container: Int = R.id.fragment_container) {
    beginTransaction()
        .replace(container, fragment, tag)
        .commit()
}

fun <T : Fragment> FragmentManager.findFragmentOrInit(tag: String, fragment: () -> T): T {
    return (findFragmentByTag(tag) ?: fragment()) as T
}

fun <T : Fragment> FragmentManager.initReplace(tag: String, container: Int = R.id.fragment_container, fragment: () -> T) {
    replace(findFragmentOrInit(tag, fragment), tag, container)
}

fun log(msg: String, tag: String = "ffnet") {
    Log.i(tag, msg)
}

fun Context.toast(msg: String, len: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, len).show()
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

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}