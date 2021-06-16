package com.gesture.avius2.ui

import com.google.mediapipe.formats.proto.LandmarkProto

interface OnPacketListener {
    fun onLandmarkPacket(direction: Int)
    fun onHandednessPacket(list: List<LandmarkProto.Landmark>)
}