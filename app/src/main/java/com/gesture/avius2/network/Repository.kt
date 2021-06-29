package com.gesture.avius2.network

import com.gesture.avius2.db.QuestionEntity
import com.gesture.avius2.db.SettingsEntity
import com.gesture.avius2.model.Question
import com.gesture.avius2.network.models.ResponseData

class Repository {

    var apiResponse: ResponseData? = null
    var questions: List<QuestionEntity> = listOf()
    var settings: SettingsEntity? = null
    var answers: List<Question> = listOf()

}