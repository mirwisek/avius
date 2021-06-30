package com.gesture.avius2.network

import com.gesture.avius2.network.ApiHelper.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {

    //TODO Remove Logging Dependencies in final version
//    private val logging = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val httpClient = OkHttpClient.Builder()
//        .addInterceptor(logging)
//        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
//        .client(httpClient)
        .build()

    var service: ApiService = retrofit.create(ApiService::class.java)

}