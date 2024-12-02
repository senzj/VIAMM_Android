package com.example.viamm.api

import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.models.getCompletedOrder.CompletedOrderResponse
import com.example.viamm.models.Analytics.AnalyticsResponse
import com.example.viamm.models.UpdateOrder.UpdateOrdersResponse
import com.example.viamm.models.getOngoingOrder.OngoingOrderResponse
import com.example.viamm.models.payment.PaymentResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

// GET REQUEST
    // RECORDS
    @GET("order/completed")
    suspend fun getCompletedOrders(
    ): Response<CompletedOrderResponse>

    // BOOKING
    @GET("order/ongoing")
    suspend fun getOngoingOrders(
    ): Response<OngoingOrderResponse>

    // ANALYTICS
    @FormUrlEncoded
    @GET("order/stats")
    fun getStats(
        @Field("date") date: String
    ): Call<AnalyticsResponse>


// POST REQUEST
    // LOGIN
    @FormUrlEncoded
    @POST("login")
    fun login(
        // the field will the the variable accepted in the server controller
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // CANCEL ORDER
    @FormUrlEncoded
    @POST("order/cancel")
    fun updateOrderCancel(
        @Field("orderId") orderId: Int,
        @Field("workstation") workstation: String,
        @Field("masseur") masseur: String
    ): Call<CancelOrderResponse>

    // PAYMENT ORDER
    @FormUrlEncoded
    @POST("order/payment")
    fun updateOrderPayment(
        @Field("orderId") orderId: Int,
        @Field("orderPayment") payment: Int,
        @Field("workstation") workstation: String,
        @Field("masseur") masseur: String
    ): Call<PaymentResponse>

}
