package com.gesture.avius2.model

data class Question(
    val index: Int,
    var id: Int,
    var question: QuestionMultiLang,
    var upAnswers: LanguageAnswer,
    var downAnswers: LanguageAnswer,
    var answer: String = ""
)

data class Answers(
    var upAnswer: LanguageAnswer = LanguageAnswer(),
    var downAnswer: LanguageAnswer = LanguageAnswer()
) {
    companion object {
        fun parse(value: String): Answers {
            val arr = value.split(",")
            val up = LanguageAnswer.parse(arr[0])
            val down = LanguageAnswer.parse(arr[1])
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
) {
    companion object {
        fun parse(value: String): QuestionMultiLang {
            val arr = value.split("\r\n")
            return QuestionMultiLang(arr[0].trim(), arr[1].trim())
        }
    }
}
