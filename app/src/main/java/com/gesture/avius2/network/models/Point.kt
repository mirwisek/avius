package com.gesture.avius2.network.models

data class Point(
    var branch_id: Int = 0,
    var created_at: String = "",
    var form: Form = Form(),
    var id: Int = 0,
    var name: String = "",
    var qrcode: String = "",
    var team_id: Int = 0,
    var text: String = "",
    var title: String = "",
    var type: String = "",
    var updated_at: String = ""
)