package com.example.viamm.models.Order

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ServiceRecord(
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

    companion object CREATOR : Parcelable.Creator<ServiceRecord> {
        override fun createFromParcel(parcel: Parcel): ServiceRecord {
            return ServiceRecord(parcel)
        }

        override fun newArray(size: Int): Array<ServiceRecord?> {
            return arrayOfNulls(size)
        }
    }
}

data class Orders(
    @SerializedName("orders_tbl_id") val orderId: String,
    @SerializedName("services") val services: Map<String, ServiceRecord>,
    @SerializedName("masseurs") val masseurs: Map<String, Boolean>,
    @SerializedName("locations") val locations: Map<String, Boolean>,
    @SerializedName("totalCost") val totalCost: Int,
    @SerializedName("orders_tbl_status") val orderStatus: String
)
