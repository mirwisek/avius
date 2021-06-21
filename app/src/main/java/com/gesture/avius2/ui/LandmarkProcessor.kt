package com.gesture.avius2.ui

import com.gesture.avius2.utils.GestureCompareUtils
import com.gesture.avius2.utils.areAdjacentParallelX
import com.gesture.avius2.utils.areParallel
import com.gesture.avius2.utils.getPoints
import com.google.mediapipe.formats.proto.LandmarkProto

object LandmarkProcessor {

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

        val dir = GestureCompareUtils.getVerticalDirection(nList)

        if(areParallel || areZipTip) {
            callback?.invoke(dir)
        }

        result += "pipDip = $areParallel and $dir\n\n"
        zipped.forEach {
            result += "P: ${it.first.x},  D: ${it.second.x}\n"
        }

        result += "\npipTip = $areZipTip and $dir\n\n"
        zipped.forEach {
            result += "P: ${it.second.x},  T: ${it.first.x}\n"
        }


        return result
    }

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