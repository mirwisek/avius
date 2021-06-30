package com.gesture.avius2.viewmodels

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gesture.avius2.App
import com.gesture.avius2.KEY_THEME
import com.gesture.avius2.db.AppDatabase
import com.gesture.avius2.db.QuestionEntity
import com.gesture.avius2.db.SettingsEntity
import com.gesture.avius2.model.Answers
import com.gesture.avius2.model.Question
import com.gesture.avius2.model.QuestionMultiLang
import com.gesture.avius2.network.models.ResponseData
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.sharedPrefs
import kotlinx.coroutines.launch

class AppViewModel(val app: Application): AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app.applicationContext, viewModelScope)

    fun saveQuestions(data: ResponseData) {
        viewModelScope.launch {
            val questions = data.point.form.questions
            val list = arrayListOf<Question>()
            val entities = arrayListOf<QuestionEntity>()
            var index = 1
            questions.forEach {
                val q = QuestionMultiLang.parse(it.question)
                val ans = Answers.parse(it.answers)
                val question = Question(index++, it.id, q, ans.upAnswer, ans.downAnswer)
                list.add(question)
                entities.add(QuestionEntity.fromQuestion(question))
            }
            (app as App).repository.questions = entities
            db.questionsDao().insert(*entities.toTypedArray())
        }
    }

    fun saveSettings(data: ResponseData) {
        viewModelScope.launch {
            val theme = data.point.form.theme_color
            // Save into shared prefs for fast retrieval
            app.applicationContext.sharedPrefs.edit(true) {
                putString(KEY_THEME, theme)
            }
            val entity = SettingsEntity(theme, data.logo)
            (app as App).repository.settings = entity
            db.settingsDao().insert(entity)
        }
    }

    fun getDbSettings() = db.settingsDao().getAllLive()

    fun getDbQuestions() = db.questionsDao().getAllLive()
}