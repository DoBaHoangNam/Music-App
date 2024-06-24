package com.example.musicapp.model

import android.os.Parcel
import android.os.Parcelable

data class Song(
    var id: String,
    val songName: String,
    val singerName: String,
    val album: String,
    val duration: Long,
    var data: String,
    val image: String
) : Parcelable {
    constructor() : this("", "", "", "", 0, "", "")
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(songName)
        parcel.writeString(singerName)
        parcel.writeString(album)
        parcel.writeLong(duration)
        parcel.writeString(data)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}
