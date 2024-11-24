package com.example.viamm.api

import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

val Devmode = "1"

object Utils {
    val BASE_URL: String = if (Devmode == "1") {
        "https://viamm.xyz/api/" // Change to your hosting URL
    } else {
        "https://IP.xyz/api/" // Change to your local IP
    }
}
