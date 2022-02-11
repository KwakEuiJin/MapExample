package com.example.part4_chapter3.response.search

data class SearchPoiInfo(
    val totalCount: String,
    val count: String,
    val page: String,
    val pois: Pois
)
