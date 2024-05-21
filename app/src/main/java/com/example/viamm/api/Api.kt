package com.example.viamm.api

import com.example.viamm.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {
//    @FormUrlEncoded
//    @POST("create_user")
//    fun login(
//        @Field("username") username: String,
//        @Field("password") password: String
//    ):Call<SignupResponse>

    @FormUrlEncoded
    @POST("Login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ):Call<LoginResponse>
}