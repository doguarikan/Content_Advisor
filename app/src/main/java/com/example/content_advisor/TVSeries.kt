package com.example.content_advisor

import com.google.gson.annotations.SerializedName
import com.example.content_advisor.model.SearchResult

data class TVSeries(
    @SerializedName("name")
    val name: String,
    @SerializedName("first_air_date")
    val first_air_date: String,
    @SerializedName("origin_country")
    val origin_country: List<String>,
    val id: Int,
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
    val title = name
    
    fun getFirstAirYear(): String = first_air_date.take(4)
    
    fun getPosterUrl(): String = "https://image.tmdb.org/t/p/w500$poster_path"
    fun getBackdropUrl(): String = "https://image.tmdb.org/t/p/w780$backdrop_path"
    
    // Convert TVSeries to SearchResult
    fun toSearchResult(): SearchResult {
        return SearchResult(
            id = id,
            mediaType = "tv",
            title = null,
            name = name,
            overview = overview,
            posterPath = poster_path,
            backdropPath = backdrop_path,
            voteAverage = vote_average,
            voteCount = vote_count,
            popularity = popularity,
            releaseDate = null,
            firstAirDate = first_air_date
        )
    }
}