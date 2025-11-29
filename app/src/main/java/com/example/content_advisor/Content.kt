package com.example.content_advisor

import com.google.gson.annotations.SerializedName

abstract class Content(
    open val id: Int,
    open val title: String,
    open val overview: String,
    @SerializedName("poster_path")
    open val poster_path: String?,
    @SerializedName("backdrop_path")
    open val backdrop_path: String?,
    @SerializedName("vote_average")
    open val vote_average: Double,
    @SerializedName("vote_count")
    open val vote_count: Int,
    open val popularity: Double,
    @SerializedName("genre_ids")
    open val genre_ids: List<Int>
) {
    abstract val contentType: ContentType

    fun getPosterUrl(): String = "https://image.tmdb.org/t/p/w500$poster_path"
    fun getBackdropUrl(): String = "https://image.tmdb.org/t/p/w780$backdrop_path"
}

enum class ContentType {
    MOVIE, TV_SERIES
}