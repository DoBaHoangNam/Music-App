package com.example.musicapp.model

import android.os.Parcel
import android.os.Parcelable

data class User (
    var id:String? = null,
    var email:String? = null,
    var username:String? = null,
    var password:String? =null,
    val image: String?
): Parcelable{
    constructor() : this("", "", "", "", "")
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(email)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}
