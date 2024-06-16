package com.example.viamm.models.getOngoingOrder

import com.google.gson.annotations.SerializedName

    data class Service(
        val price: Int,
        val amount: Int,
        val type: String
    )

    data class Masseur(
        val name: String,
        val isAvailable: Boolean
    )

    data class Location(
        val name: String,
        val isAvailable: Boolean
    )

    data class OngoingOrder(
        @SerializedName("orders_tbl_id") val orderId: String,
        @SerializedName("services") val services: Map<String, Service>,
        @SerializedName("masseurs") val masseurs: Map<String, Boolean>,
        @SerializedName("locations") val locations: Map<String, Boolean>,
        @SerializedName("totalCost") val totalCost: Int,
        @SerializedName("orders_tbl_status") val orderStatus: String
    )

