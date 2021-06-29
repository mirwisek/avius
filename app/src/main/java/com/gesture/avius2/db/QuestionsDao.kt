package com.gesture.avius2.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionsDao {

    // Suspend puts must condition on running these methods Asynchronously
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg questions: QuestionEntity)

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    fun getAllLive(): LiveData<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id LIKE :id ")
    suspend fun getQuestionById(id: String): QuestionEntity

    @Query("DELETE FROM questions")
    suspend fun deleteAll()
}