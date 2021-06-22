package com.gesture.avius2.network

import com.gesture.avius2.network.models.ResponseData
import junit.framework.TestCase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch

class ApiHelperTest : TestCase() {

    public override fun setUp() {
        super.setUp()
    }

    fun testGetQuestions() {
        val latch = CountDownLatch(1)
//        RetrofitBuilder.prepare().getQuestions()
        ApiHelper.getQuestions(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {

                println("Status: ${response.code()}")
                println(response.body()?.logo)
                assertEquals(response.body()?.logo?.isNotEmpty(), true)
                latch.countDown()
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                latch.countDown()
            }

        })

        try {
            latch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}