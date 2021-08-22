package com.gesture.avius2.ui

import com.gesture.avius2.Point
import com.gesture.avius2.utils.*
import com.google.mediapipe.formats.proto.LandmarkProto

object LandmarkProcessor {

//    fun isThumbTipUp(
//        nList: List<LandmarkProto.NormalizedLandmark>,
//        callback: ((direction: Int) -> Unit)? = null
//    ) {
//        val tl = nList.getPoints(GestureCompareUtils.THUMB_LINE).toMutableList()
//        return tl[0]
//    }

    fun isThumbTipUp(
        nList: List<LandmarkProto.NormalizedLandmark>,
        callback: ((direction: Int) -> Unit)? = null
    ) {
        // 0=TIP, 1=DIP, 2=PIP, 3=MCP (TOP to Bottom -> 0-3)
        /**
         * The main logic is, the top of stack must be minimum
         */
        val tl = nList.getPoints(GestureCompareUtils.THUMB_LINE).toMutableList()
        var minChecks = 0
        var maxChecks = 0
        for(i in 0..3) {
            val isTopMin = tl.minOf { it.y } == tl[0].y
            val isTopMax = tl.maxOf { it.y } == tl[0].y
            if(isTopMin)
                minChecks++
            else if(isTopMax)
                maxChecks++
            tl.removeAt(0)  // Remove first element
        }
        val res = when {
            minChecks >= 3 -> 1     // When all are minimum (ThumbsUp)
            maxChecks >= 3 -> -1    // When all are maximum (ThumbsDown)
            else -> 0               // Some other gesture
        }
        callback?.invoke(res)
        // Just for debug
//        return "minChecks = $minChecks and maxChecks = $maxChecks"
    }

    fun processZAxis(
        nList: List<LandmarkProto.NormalizedLandmark>,
        callback: ((areFingersCurled: Boolean) -> Unit)? = null
    ): String {
        val index = nList.getPoints(GestureCompareUtils.INDEX)
        val middle = nList.getPoints(GestureCompareUtils.MIDDLE)
        val ring = nList.getPoints(GestureCompareUtils.RING)
        val pinky = nList.getPoints(GestureCompareUtils.PINKY)
        val palm = nList.getPoints(GestureCompareUtils.PALM)[0]
        val curlList = listOf(
//            isFingerCurled(index, palm),
            isFingerCurled(middle, palm),
            isFingerCurled(ring, palm),
//            isFingerCurled(pinky, palm)
        )
        callback?.invoke(curlList.all { it })

//        return "index: ${curlList[0]}\nmiddle: ${curlList[1]}\nring: ${curlList[2]}\npinky: ${curlList[3]}"
//        return "middle: ${curlList[0]}\nring: ${curlList[1]}\n}"
        return ""
    }

    private fun isFingerCurled(list: List<Point>, palm: Point): Boolean {
        return list[0].z > list[2].z && list[0].z > list[3].z
                && palm.z > list[0].z
    }

    @Deprecated("Biased with up direction and would ignore downwards dir in most cases")
    // Works well to detect close hand but biased with thumb up
    fun process2(
        nList: List<LandmarkProto.NormalizedLandmark>,
        callback: ((direction: Int) -> Unit)? = null
    ): String {
        var result = ""

        val tip = nList.getPoints(GestureCompareUtils.TIP)
        val dip = nList.getPoints(GestureCompareUtils.DIP)
        val pip = nList.getPoints(GestureCompareUtils.PIP)
        val mcp = nList.getPoints(GestureCompareUtils.MCP)

        val zipped = pip.zip(dip)
        val areParallel = zipped.areAdjacentParallelX(2.5F..20F)

        // When hand is rotated a bit
        val zipTip = tip.zip(pip)
        val areZipTip = zipTip.areAdjacentParallelX(2.5F..20F)

//        val dir = GestureCompareUtils.getVerticalDirection(nList)

        if(areParallel || areZipTip) {
//            callback?.invoke(dir)
            result += isThumbTipUp(nList, callback)
        }

//        result += "pipDip = $areParallel and $dir\n\n"
//        zipped.forEach {
//            result += "P: ${it.first.x},  D: ${it.second.x}\n"
//        }
//
//        result += "\npipTip = $areZipTip and $dir\n\n"
//        zipped.forEach {
//            result += "P: ${it.second.x},  T: ${it.first.x}\n"
//        }


        return result
    }

    @Deprecated("Doesn't work well with thumb down")
    fun process(
        nList: List<LandmarkProto.NormalizedLandmark>,
        packetListeners: HashMap<String, OnPacketListener>
    ) {

        val dir = GestureCompareUtils.getVerticalDirection(nList)

        val tip = nList.getPoints(GestureCompareUtils.TIP)
        val dip = nList.getPoints(GestureCompareUtils.DIP)
        val pip = nList.getPoints(GestureCompareUtils.PIP)
        val mcp = nList.getPoints(GestureCompareUtils.MCP)
        val thumbLine = nList.getPoints(GestureCompareUtils.THUMB_LINE)

//        log("Tip size is ${tip.strLine(GestureCompareUtils.TIP) }")

//        val isTip = tip.areParallel()
//        val isDip = dip.areParallel()
//        val isPip = pip.areParallel()
//        val isMcp = mcp.areParallel()
        val isThumbTIP = (thumbLine + tip).areParallel(0.2F)
        val isThumbDIP = (thumbLine + dip).areParallel(0.2F)
        val isThumbPIP = (thumbLine + pip).areParallel(0.2F)
        val isThumbMCP = (thumbLine + mcp).areParallel(0.2F)

        var dirInt = 0

        // For label print purposes only
        val m =
            if (isThumbMCP) "mcp" else if (isThumbDIP) "dip" else if (isThumbPIP) "pip" else "tip"
        val res =
            if (/*isTip && isDip && isPip && isMcp*/isThumbMCP || isThumbPIP || isThumbDIP || isThumbTIP) {
                "THUMB $dir" /*+ "($m)"*/
            } else {
                dirInt = 0
                "NO THUMB"
            }
        packetListeners.forEach { (_, v) ->
            v.onLandmarkPacket(dirInt)
        }
//        vmMain.updateThumbStatus(dirInt)


//        runOnUiThread {

//            labelPoints.text = res + "\n"

//                tip.strLine(GestureCompareUtils.TIP) + "\n\n" +
//                dip.strLine(GestureCompareUtils.DIP) + "\n\n" +
//                pip.strLine(GestureCompareUtils.PIP) + "\n\n" +
//                mcp.strLine(GestureCompareUtils.MCP)
//        }
    }

}