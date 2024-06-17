package com.example.viamm.models.getOngoingOrder

import com.google.gson.annotations.SerializedName
import android.os.Parcel
import android.os.Parcelable


    data class ServiceOrder(
        val amount: Int,
        val name: String,
        val price: Int,
        val type: String
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(amount)
            parcel.writeString(name)
            parcel.writeInt(price)
            parcel.writeString(type)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ServiceOrder> {
            override fun createFromParcel(parcel: Parcel): ServiceOrder {
                return ServiceOrder(parcel)
            }

            override fun newArray(size: Int): Array<ServiceOrder?> {
                return arrayOfNulls(size)
            }
        }
    }

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
    @SerializedName("services") val services: Map<String, ServiceOrder>,
    @SerializedName("masseurs") val masseurs: Map<String, Boolean>,
    @SerializedName("locations") val locations: Map<String, Boolean>,
    @SerializedName("totalCost") val totalCost: Int,
    @SerializedName("orders_tbl_status") val orderStatus: String
)

