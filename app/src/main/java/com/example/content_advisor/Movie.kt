package com.example.content_advisor

import com.google.gson.annotations.SerializedName
import com.example.content_advisor.model.SearchResult

data class Movie(
    @SerializedName("release_date")
    val release_date: String,
    val adult: Boolean,
    @SerializedName("original_language")
    val original_language: String,
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val poster_path: String?,
    @SerializedName("backdrop_path")
    val backdrop_path: String?,
    @SerializedName("vote_average")
    val vote_average: Double,
    @SerializedName("vote_count")
    val vote_count: Int,
    val popularity: Double,
    @SerializedName("genre_ids")
    val genre_ids: List<Int>
) {
    fun getReleaseYear(): String = release_date.take(4)
    
    fun getPosterUrl(): String = "https://image.tmdb.org/t/p/w500$poster_path"
    fun getBackdropUrl(): String = "https://image.tmdb.org/t/p/w780$backdrop_path"
    
    // Convert Movie to SearchResult
    fun toSearchResult(): SearchResult {
        return SearchResult(
            id = id,
            mediaType = "movie",
            title = title,
            name = null,
            overview = overview,
            posterPath = poster_path,
            backdropPath = backdrop_path,
            voteAverage = vote_average,
            voteCount = vote_count,
            popularity = popularity,
            releaseDate = release_date,
            firstAirDate = null
        )
    }
}
