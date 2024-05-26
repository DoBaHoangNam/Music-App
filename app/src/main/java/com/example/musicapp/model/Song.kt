package com.example.musicapp.model

import android.os.Parcel
import android.os.Parcelable

data class Song(
    val id: Long,
    val songName: String,
    val singerName: String,
    val album: String,
    val duration: Long,
    val data: String,
    val image: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
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
