package com.gesture.avius2.network

import com.gesture.avius2.model.Question
import com.gesture.avius2.network.models.ResponseData

class Repository {

    var questions: ResponseData? = null
    var themeColor: String = ""
    var answers: List<Question> = listOf()

}