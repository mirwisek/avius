package com.gesture.avius2.model

data class Question(
    val index: Int,
    var id: Int,
    var question: QuestionMultiLang,
    var upAnswers: String,
    var downAnswers: String,
    var answer: String = ""
)

data class Answers(
    var upAnswer: String = "",
    var downAnswer: String = ""
) {
    companion object {
        fun parse(value: String): Answers {
            val arr = value.split(",")
//            val up = LanguageAnswer.parse(arr[0])
//            val down = LanguageAnswer.parse(arr[1])
            val up = arr[0]
            val down = arr[1]
            return Answers(up, down)
        }
    }
}

data class LanguageAnswer(
    var english: String = "",
    var arabic: String = "",
) {
    companion object {
        fun parse(value: String): LanguageAnswer {
            val arr = value.split("-")
            return LanguageAnswer(arr[0].trim(), arr[1].trim())
        }
    }
}

data class QuestionMultiLang(
    var english: String = "",
    var arabic: String = "",
    var absoluteValue: String = ""
) {
    companion object {
        fun parse(value: String): QuestionMultiLang {
            val arr = value.split("\r\n")
            return QuestionMultiLang(arr[0].trim(), arr[1].trim(), value)
        }
    }
}
