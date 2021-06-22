package com.gesture.avius2.network.models

data class Form(
    var created_at: String = "",
    var fields: String = "",
    var id: Int = 0,
    var point_id: Int = 0,
    var questions: List<Question> = listOf(),
    var rate_label: String? = null,
    var submit_text: String = "",
    var theme_color: String = "",
    var updated_at: String = ""
)