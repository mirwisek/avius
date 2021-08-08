package com.gesture.avius2.network

import com.gesture.avius2.network.models.AnswerResponse
import com.gesture.avius2.network.models.ResponseData
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/api/questions/{point_id}")
    fun getQuestions(@Path("point_id") pointId: String, @Body body: RequestBody): Call<ResponseData>

    @POST("/api/answers/{point_id}")
    fun submitAnswer(@Path("point_id") pointId: String, @Body body: RequestBody): Call<AnswerResponse>

    // Ignore the rest of the Json parameters, that is why not creating a separate data class
    @POST("/api/user/logout")
    fun logout(@Body body: RequestBody): Call<AnswerResponse>

}