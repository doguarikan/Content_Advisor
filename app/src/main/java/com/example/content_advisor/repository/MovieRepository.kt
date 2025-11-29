package com.example.content_advisor.repository

import com.example.content_advisor.BuildConfig
import com.example.content_advisor.api.RetrofitClient
import com.example.content_advisor.Movie
import com.example.content_advisor.TVSeries
import com.example.content_advisor.model.TMDBResponse
import com.example.content_advisor.model.SearchResult
import com.example.content_advisor.model.Credits

class MovieRepository {
    private val apiService = RetrofitClient.tmdbApiService

    private val API_KEY = BuildConfig.TMDB_API_KEY

    suspend fun searchMulti(query: String): TMDBResponse<SearchResult> {
        return apiService.searchMulti(query = query, apiKey = API_KEY)
    }

    suspend fun getPopularMovies(): TMDBResponse<Movie> {
        return apiService.getPopularMovies(apiKey = API_KEY)
    }

    suspend fun getPopularTVSeries(): TMDBResponse<TVSeries> {
        return apiService.getPopularTVSeries(apiKey = API_KEY)
    }

    suspend fun getMovieDetails(movieId: Int): Movie {
        return apiService.getMovieDetails(movieId, apiKey = API_KEY)
    }

    suspend fun getTVSeriesDetails(tvId: Int): TVSeries {
        return apiService.getTVSeriesDetails(tvId, apiKey = API_KEY)
    }

    suspend fun getMovieCredits(movieId: Int): Credits {
        return apiService.getMovieCredits(movieId, apiKey = API_KEY)
    }

    suspend fun getTVSeriesCredits(tvId: Int): Credits {
        return apiService.getTVSeriesCredits(tvId, apiKey = API_KEY)
    }

    suspend fun getRecommendedTVSeries(tvId: Int): TMDBResponse<TVSeries> {
        return apiService.getRecommendedTVSeries(tvId, apiKey = API_KEY)
    }

    suspend fun getRecommendedMovies(movieId: Int): TMDBResponse<Movie> {
        return apiService.getRecommendedMovies(movieId, apiKey = API_KEY)
    }
} 