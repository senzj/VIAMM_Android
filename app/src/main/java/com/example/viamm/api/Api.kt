package com.example.viamm.api

import com.example.viamm.models.Delete.DeleteOrderResponse
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.models.Order.OrderResponse
import com.example.viamm.models.Update.UpdateOrdersResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("order")
    suspend fun getAllOrders(

    ): Response<OrderResponse>

    @FormUrlEncoded
    @POST("order/update")
    fun updateOrder(
        @Field("orderId") orderId: String,
        @Field("orderService") service: String,
        @Field("orderEmpName") empName: String,
        @Field("orderStatus") status: String
    ): Call<UpdateOrdersResponse>

    @FormUrlEncoded
    @POST("order/delete")
    fun deleteOrder(
        @Field("orderId") orderId: String
    ): Call<DeleteOrderResponse>
}
