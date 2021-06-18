package com.gesture.avius2.utils

import com.gesture.avius2.Point
import com.google.mediapipe.formats.proto.LandmarkProto
import kotlin.math.abs

object GestureCompareUtils {

    const val TIP = 1
    const val DIP = 2
    const val PIP = 3
    const val MCP = 4
    const val THUMB_LINE = 5

    fun getVerticalDirection(nList: List<LandmarkProto.NormalizedLandmark>): String {
        return if(nList[4].y < nList[0].y) {
            "UP"
        } else {
            "DN"
        }
    }

}

fun List<LandmarkProto.NormalizedLandmark>.getPoints(which: Int, decimalPlace:Int = 1): List<Point> {
    val list = when(which) {
        // 8,12,16,20
        GestureCompareUtils.TIP -> listOf(
            get(8), get(12), get(16), get(20)
        )
        // 7,11,15,19
        GestureCompareUtils.DIP -> listOf(
            get(7), get(11), get(15), get(19)
        )
        // 6,10,14,18
        GestureCompareUtils.PIP -> listOf(
            get(6), get(10), get(14), get(18)
        )
        // 5,9,13,17
        GestureCompareUtils.MCP -> listOf(
            get(5), get(9), get(13), get(17)
        )
        // 4,3,2
        GestureCompareUtils.THUMB_LINE -> listOf(
            get(4), get(3), get(2)
        )
        // 0
        else -> listOf(get(0))
    }
    return list.map {
        Point(it.x.roundTo(decimalPlace), it.y.roundTo(decimalPlace), it.z.roundTo(decimalPlace))
    }
}

fun List<Point>.areParallel(threshold: Float = 0.1F, isX: Boolean = true): Boolean {
    val maxTip = maxOf { if (isX) it.x else it.y }
    val minTip = minOf { if (isX) it.x else it.y }
    val diffTip = abs(maxTip - minTip)
    return diffTip <= threshold
}

fun List<Point>.strLine(which: Int): String {
    var result = ""
    when(which) {
        GestureCompareUtils.TIP -> {
            result =
                """
                    [08] ${this[0].x} ,${this[0].y}
                    [12] ${this[1].x} ,${this[1].y}
                    [16] ${this[2].x} ,${this[2].y}
                    [20] ${this[3].x} ,${this[3].y}
                """.trimIndent()
        }
        GestureCompareUtils.DIP -> {
            result =
                """
                    [07] ${this[0].x} ,${this[0].y}
                    [11] ${this[1].x} ,${this[1].y}
                    [15] ${this[2].x} ,${this[2].y}
                    [19] ${this[3].x} ,${this[3].y}
                """.trimIndent()
        }
        GestureCompareUtils.PIP -> {
            result =
                """
                    [06] ${this[0].x} ,${this[0].y}
                    [10] ${this[1].x} ,${this[1].y}
                    [14] ${this[2].x} ,${this[2].y}
                    [18] ${this[3].x} ,${this[3].y}
                """.trimIndent()
        }
        GestureCompareUtils.MCP -> {
            result =
                """
                    [05] ${this[0].x} ,${this[0].y}
                    [09] ${this[1].x} ,${this[1].y}
                    [13] ${this[2].x} ,${this[2].y}
                    [17] ${this[3].x} ,${this[3].y}
                """.trimIndent()
        }
    }

    return result
}