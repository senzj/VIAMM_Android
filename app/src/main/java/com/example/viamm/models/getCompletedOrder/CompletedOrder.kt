package com.example.viamm.models.getCompletedOrder

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// Data class for ServiceRecord

data class ServiceRecord(
    val name: String,
    val price: Int,
    val amount: Int,
    val type: String,
    val locations: String
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

// Data class for Masseur
data class Masseur(
    // JSON key "name" for masseur maps to variable "masseurName"
    @SerializedName("name") val masseurName: String,
    @SerializedName("gender") val masseurGender: String,
    @SerializedName("is_assigned") val isAssigned: Boolean
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(masseurName)
        parcel.writeString(masseurGender)
        parcel.writeByte(if (isAssigned) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Masseur> {
        override fun createFromParcel(parcel: Parcel): Masseur {
            return Masseur(parcel)
        }

        override fun newArray(size: Int): Array<Masseur?> {
            return arrayOfNulls(size)
        }
    }
}

// Data class for Customer
data class Customer(
    @SerializedName("name") val customerName: String,
    @SerializedName("contact") val customerContact: String,
    @SerializedName("gender") val customerGender: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(customerName)
        parcel.writeString(customerContact)
        parcel.writeString(customerGender)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Customer> {
        override fun createFromParcel(parcel: Parcel): Customer {
            return Customer(parcel)
        }

        override fun newArray(size: Int): Array<Customer?> {
            return arrayOfNulls(size)
        }
    }
}

// Data class for Orders
data class CompletedOrder(
    // @SerializedName("key_from_api_response") val variableName: VariableType

    @SerializedName("id") val orderId: String,
    @SerializedName("status") val orderStatus: String,
    @SerializedName("services") val services: ServiceRecord,
    @SerializedName("masseur") val masseur: Masseur,
    @SerializedName("customer") val customer: Customer,
    @SerializedName("workstation") val workstation: String,
    @SerializedName("totalCost") val totalCost: String, // Changed to String to handle "N/A"
    @SerializedName("paid_amount") val paidAmount: String,
    @SerializedName("date") val orderDate: String,
    @SerializedName("time_end") val timeEnd: String
)
