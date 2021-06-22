package com.gesture.avius2.network.models

data class ResponseData(
    var status: String = "", // In case of Error
    var msg: String = "",    // In case of Error
    var logo: String = "",
    var point: Point = Point()
)