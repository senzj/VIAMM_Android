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
        "http://192.168.254.105/Capstoneproject_web/api/" // Change to your local IP
    }

    val API_KEY = "viamm_mo_key_bile"
}
