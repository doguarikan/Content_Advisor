package com.example.content_advisor.api

import com.example.content_advisor.BuildConfig
import com.example.content_advisor.Movie
import com.example.content_advisor.TVSeries
import com.example.content_advisor.model.TMDBResponse
import com.example.content_advisor.model.SearchResult
import com.example.content_advisor.model.Credits
import com.example.content_advisor.model.GenreResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse<Movie>

    @GET("tv/popular")
    suspend fun getPopularTVSeries(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse<TVSeries>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Movie

    @GET("tv/{tv_id}")
    suspend fun getTVSeriesDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): TVSeries

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Credits

    @GET("tv/{tv_id}/credits")
    suspend fun getTVSeriesCredits(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Credits

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TMDBResponse<SearchResult>

    @GET("tv/{tv_id}/recommendations")
    suspend fun getRecommendedTVSeries(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse<TVSeries>

    @GET("movie/{movie_id}/recommendations")
    suspend fun getRecommendedMovies(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TMDBResponse<Movie>
}

