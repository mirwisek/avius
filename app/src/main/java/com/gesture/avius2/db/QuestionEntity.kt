package com.gesture.avius2.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.gesture.avius2.model.Question
import com.gesture.avius2.model.QuestionMultiLang

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val index: Int,
    var question: String,
    var upAnswers: String,
    var downAnswers: String
) {

    companion object {
        fun fromQuestion(q: Question): QuestionEntity {
            return QuestionEntity(q.id, q.index, q.question.absoluteValue, q.upAnswers, q.downAnswers)
        }
    }

    @Ignore
    fun toQuestion(): Question {
        return Question(index, id, QuestionMultiLang.parse(question), upAnswers, downAnswers)
    }
}