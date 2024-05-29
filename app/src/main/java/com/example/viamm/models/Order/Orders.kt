package com.example.viamm.models.Order

import com.google.gson.annotations.SerializedName

data class Orders(
    @SerializedName("orders_tbl_id") val orderId: String,
    @SerializedName("orders_tbl_services") val orderService: String,
    @SerializedName("orders_tbl_empName") val orderEmpName: String,
    @SerializedName("orders_tbl_status") val orderStatus: String
)
