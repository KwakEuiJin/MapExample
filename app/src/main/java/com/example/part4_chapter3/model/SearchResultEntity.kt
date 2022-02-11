package com.example.part4_chapter3.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResultEntity(
    val fullAdress:String,
    val name:String,
    val locationLatLng:LocationLatLngEntity
):Parcelable
