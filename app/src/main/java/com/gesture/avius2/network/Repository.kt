package com.gesture.avius2.network

import com.gesture.avius2.db.QuestionEntity
import com.gesture.avius2.model.Question
import com.gesture.avius2.network.models.ResponseData

class Repository {

    var apiResponse: ResponseData? = null
    var questions: List<QuestionEntity> = listOf()
    var themeColor: String = ""
    var answers: List<Question> = listOf()

}