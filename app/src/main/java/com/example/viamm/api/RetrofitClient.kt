package com.example.viamm.api

import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.254.145/Capstoneproject_web/api/" // change IP address to your local

    // Encode username and password using OkHttp's Credentials utility
    private val basicAuth = Credentials.basic("admin", "1234")

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .addHeader("X-API-KEY", "viamm_key") // Add the required X-API-KEY header
                    .addHeader("Authorization", basicAuth) // Add the Basic Auth header
                    .method(original.method(), original.body())
                val request = requestBuilder.build()
                chain.proceed(request)
            }.build()
    }

    val instance: Api by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        retrofit.create(Api::class.java)
    }
}
