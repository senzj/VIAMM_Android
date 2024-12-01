package com.example.viamm.api

import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.models.getCompletedOrder.CompletedOrderResponse
import com.example.viamm.models.Analytics.AnalyticsResponse
import com.example.viamm.models.UpdateOrder.UpdateOrdersResponse
import com.example.viamm.models.getOngoingOrder.OngoingOrderResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    // API to handle data input by user and sending it to server controller
    @FormUrlEncoded
    @POST("login")
    fun login(
        // the field will the the variable accepted in the server controller
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // API to get all completed order
    @GET("order/completed")
    suspend fun getCompletedOrders(
    ): Response<CompletedOrderResponse>

    @GET("order/ongoing")
    suspend fun getOngoingOrders(
    ): Response<OngoingOrderResponse>

    // API to handle data updated by user and sending it to server controller
    @FormUrlEncoded
    @POST("order/update")
    fun updateOrder(
        @Field("orderId") orderId: String,
        @Field("orderService") service: String,
        @Field("orderEmpName") empName: String,
        @Field("orderStatus") status: String
    ): Call<UpdateOrdersResponse>

    // API to handle data updated status by user and sending it to server controller
    @FormUrlEncoded
    @POST("order/update")
    fun updateOrderStatus(
        @Field("orderId") orderId: String,
        @Field("orderStatus") status: String
    ): Call<CancelOrderResponse>

    // API handle statistics data to display to the graph
    @FormUrlEncoded
    @GET("order/stats")
    fun getStats(
        @Field("date") date: String
    ): Call<AnalyticsResponse>

    // API to fetch and display
}
