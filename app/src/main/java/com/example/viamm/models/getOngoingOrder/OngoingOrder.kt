package com.example.viamm.models.getOngoingOrder

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// Data class for ServiceOrder
data class ServiceOrder(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("amount") val amount: Int,
    @SerializedName("type") val type: String,
    @SerializedName("locations") val locations: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(price)
        parcel.writeInt(amount)
        parcel.writeString(type)
        parcel.writeString(locations)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ServiceOrder> {
        override fun createFromParcel(parcel: Parcel): ServiceOrder = ServiceOrder(parcel)
        override fun newArray(size: Int): Array<ServiceOrder?> = arrayOfNulls(size)
    }
}

// Data class for Masseur
data class Masseur(
    // JSON key "name" for masseur maps to variable "masseurName"
    @SerializedName("name") val masseurName: String,
    @SerializedName("gender") val masseurGender: String,
    @SerializedName("is_assigned") val isAssigned: Boolean
)

// Data class for Customer
data class Customer(
    @SerializedName("name") val customerName: String,
    @SerializedName("contact") val customerContact: String,
    @SerializedName("gender") val customerGender: String
)

// Data class for OngoingOrder
data class OngoingOrder(
    // @SerializedName("key_from_api_response") val variableName: VariableType

    @SerializedName("id") val orderId: String,
    @SerializedName("status") val orderStatus: String,
    @SerializedName("services") val services: ServiceOrder,
    @SerializedName("masseur") val masseur: Masseur,
    @SerializedName("customer") val customer: Customer, // Make sure this is an object, not an array
    @SerializedName("workstation") val workstation: String,
    @SerializedName("totalCost") val totalCost: String,
    @SerializedName("paid_amount") val paidAmount: String,
    @SerializedName("date") val orderDate: String,
    @SerializedName("time_end") val timeEnd: String
)