package com.gesture.avius2.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.gesture.avius2.App
import com.gesture.avius2.db.AppDatabase
import com.gesture.avius2.model.Question
import kotlinx.coroutines.launch

class QuestionViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = (app as App).repository
    private val repoQuestions = MutableLiveData(repository.questions)
    val themeColor = repository.settings?.themeColor ?: ""

    val progressTimeout = MutableLiveData(0)
    val qStartTime = MutableLiveData<Long>()

    val questions = Transformations.map(repoQuestions) { ques ->
        val list = arrayListOf<Question>()
        ques?.forEach {
            list.add(it.toQuestion())
        }
        list
    }

    fun saveAnswer(index: Int, answer: String) {
        questions.value?.getOrNull(index)?.answer = answer
    }

    fun storeAnswers() {
        questions.value!!.let {
            repository.answers = it
        }
    }

    val currentQuestion = MutableLiveData<Question>()

    // Recognition
    val thumbStatus = MutableLiveData(0)
    val progressBarUp = MutableLiveData(0)
    val progressBarDown = MutableLiveData(0)
    val handDetectedLastTimestamp = MutableLiveData<Long>()

    fun nextQuestion(): Boolean {
        var nextQ: Question? = null
        currentQuestion.value?.let {
            // Because index is 1 based so it will get nextQuestion in case of list
            // getOrNull will not throw exception rather return null on index exceeding range
            nextQ = questions.value!!.getOrNull(it.index)
        }
        return if (nextQ == null) {
            false
        } else {
            currentQuestion.postValue(nextQ!!)
            true
        }
    }

    fun tick() {
        viewModelScope.launch {
            if (thumbStatus.value == 1) {
                progressBarUp.value = progressBarUp.value!!.plus(3)
                progressBarDown.value = 0   // Reset the other
            } else if (thumbStatus.value == -1) {
                progressBarDown.value = progressBarDown.value!!.plus(3)
                progressBarUp.value = 0   // Reset the other
            }
        }
    }

    fun resetViewModel() {
        thumbStatus.postValue(0)
        progressBarDown.postValue(0)
        progressBarUp.postValue(0)
        handDetectedLastTimestamp.postValue(0)
    }

}