package com.example.content_advisor.model

import com.google.gson.annotations.SerializedName

data class SearchResult(
    val id: Int,
    @SerializedName("media_type")
    val mediaType: String,
    val title: String? = null,
    val name: String? = null,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val popularity: Double,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("first_air_date")
    val firstAirDate: String? = null
) {
    fun getDisplayTitle(): String = title ?: name ?: "Unknown"
    fun getYear(): String = (releaseDate ?: firstAirDate ?: "").take(4)
    fun getPosterUrl(): String = "https://image.tmdb.org/t/p/w500$posterPath"
    fun getBackdropUrl(): String = "https://image.tmdb.org/t/p/w780$backdropPath"
    fun isMovie(): Boolean = mediaType == "movie"
    fun isTVSeries(): Boolean = mediaType == "tv"
} 