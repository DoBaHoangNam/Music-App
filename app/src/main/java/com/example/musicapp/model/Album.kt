package com.example.musicapp.model

import android.os.Parcel
import android.os.Parcelable

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val numberOfSongs: Int,
    val albumArt: String,
    val songs: List<Song>? // Thêm danh sách các bài hát
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,

        parcel.readInt(),
        parcel.readString()!!,
        parcel.createTypedArrayList(Song) // Đọc danh sách các bài hát từ Parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeInt(numberOfSongs)
        parcel.writeString(albumArt)
        parcel.writeTypedList(songs) // Ghi danh sách các bài hát vào Parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }
}


