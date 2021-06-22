package com.gesture.avius2.model

data class Question(
    var id: Int = -1,
    var question: String = "",
    var upLabel: String = "",
    var downLabel: String = "",
    var answer: String = ""
)
