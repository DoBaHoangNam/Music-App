package com.example.musicapp.model

import android.os.Parcel
import android.os.Parcelable

data class SongInAlbum(
    val id: Long,
    val title: String,
    val duration: Long,
    val artist: String,
    val data: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeLong(duration)
        parcel.writeString(artist)
        parcel.writeString(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongInAlbum> {
        override fun createFromParcel(parcel: Parcel): SongInAlbum {
            return SongInAlbum(parcel)
        }

        override fun newArray(size: Int): Array<SongInAlbum?> {
            return arrayOfNulls(size)
        }
    }
}
