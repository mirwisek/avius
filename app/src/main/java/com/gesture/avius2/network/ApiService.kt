package com.gesture.avius2.network

import com.gesture.avius2.network.models.AnswerResponse
import com.gesture.avius2.network.models.ResponseData
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/api/questions/11")
    fun getQuestions(@Body body: RequestBody): Call<ResponseData>

    @POST("/api/answers/7")
    fun submitAnswer(@Body body: RequestBody): Call<AnswerResponse>

}