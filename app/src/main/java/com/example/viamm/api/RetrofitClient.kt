package com.example.viamm.api

import androidx.media3.common.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Encode username and password using OkHttp's Credentials utility
    private val basicAuth = Credentials.basic(
        "aDm1n%v1AmM", // Username
        "1A7*ajHy6p\$ag5" // Password
    )

    // OkHttpClient with Interceptors for logging and authentication
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY // Log everything in debug
            } else {
//                HttpLoggingInterceptor.Level.BASIC // Minimal logging in release
                HttpLoggingInterceptor.Level.BODY
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .addHeader("X-API-KEY", Utils.API_KEY) // Add the required X-API-KEY header
                    .addHeader("Authorization", basicAuth) // Add the Basic Auth header
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }

    // Create a custom Gson instance with lenient parsing
    private val gson = GsonBuilder()
        .setLenient() // Make Gson lenient to malformed JSON
        .create()

    // Retrofit instance
    val instance: Api by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(Utils.BASE_URL) // Use the base URL from your Utils object
            .addConverterFactory(GsonConverterFactory.create(gson)) // Use custom Gson
            .client(okHttpClient)
            .build()
        retrofit.create(Api::class.java)
    }
}
