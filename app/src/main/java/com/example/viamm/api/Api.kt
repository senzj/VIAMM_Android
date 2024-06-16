package com.example.viamm.api

import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.models.Order.OrderResponse
import com.example.viamm.models.Statistics.StatisticResponse
import com.example.viamm.models.UpdateOrder.UpdateOrdersResponse
import com.example.viamm.models.getOngoingOrder.OngoingOrderResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    // API to handle data inputed by user and sending it to codeginiter
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // API to handle data requested by the user and retrieve via codeigniter
    @GET("order/completed")
    suspend fun getCompletedOrders(

    ): Response<OrderResponse>

    @GET("order/ongoing")
    suspend fun getOngoingOrders(

    ): Response<OngoingOrderResponse>

    // API to handle data updated by user and sending it to codeginiter
    @FormUrlEncoded
    @POST("order/update")
    fun updateOrder(
        @Field("orderId") orderId: String,
        @Field("orderService") service: String,
        @Field("orderEmpName") empName: String,
        @Field("orderStatus") status: String
    ): Call<UpdateOrdersResponse>

    // API to handle data updated status by user and sending it to codeginiter
    @FormUrlEncoded
    @POST("order/cancelled")
    fun updateOrderStatus(
        @Field("orderId") orderId: String,
        @Field("orderStatus") status: String
    ): Call<CancelOrderResponse>

    // API handle statistics data to display to the graph
    @FormUrlEncoded
    @GET("order/stats")
    fun getStats(
        @Field("date") date: String
    ): Call<StatisticResponse>
}
