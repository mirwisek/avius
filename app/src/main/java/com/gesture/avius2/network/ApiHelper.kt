package com.gesture.avius2.network

import com.gesture.avius2.model.Question
import com.gesture.avius2.network.models.AnswerResponse
import com.gesture.avius2.network.models.ResponseData
import com.gesture.avius2.utils.log
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiHelper {

    private val service = RetrofitBuilder.service
    private const val COMPANY_ID = "IMH0010"

    const val BASE_URL = "https://imhappy.sa/"
    const val IMAGES_URL = "$BASE_URL/images/"

    fun getQuestions(companyId: String, pointId: String, onResult: (Result<ResponseData>) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("company_id", companyId)
            .build()

        service.getQuestions(pointId, requestBody).enqueue(object: Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                val body = response.body()
                if(response.isSuccessful && body != null && !response.body()!!.status.contains("fail")) {
                    onResult(Result.success(body))
                } else {
                    onResult(Result.failure(Exception(body?.msg)))
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                onResult(Result.failure(t))
            }

        })
    }

    fun submitAnswer(answers: List<Question>, pointId: String, callback: Callback<AnswerResponse>) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM).apply {
                answers.forEach {
                    addFormDataPart("answer[${it.id}]", it.answer)
                }
            }.build()

        service.submitAnswer(pointId, requestBody).enqueue(callback)
    }

    fun logout(email: String, password: String, callback: Callback<AnswerResponse>) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM).apply {
                addFormDataPart("email", email)
                addFormDataPart("password", password)
            }.build()

        service.logout(requestBody).enqueue(callback)
    }
}