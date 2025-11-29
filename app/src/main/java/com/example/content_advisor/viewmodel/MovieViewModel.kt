package com.example.content_advisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.content_advisor.Movie
import com.example.content_advisor.TVSeries
import com.example.content_advisor.Content
import com.example.content_advisor.ContentType
import com.example.content_advisor.model.SearchResult
import com.example.content_advisor.repository.MovieRepository
import kotlinx.coroutines.launch
import com.example.content_advisor.model.Credits

class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()
    
    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    private val _popularMovies = MutableLiveData<List<com.example.content_advisor.model.SearchResult>>()
    val popularMovies: LiveData<List<com.example.content_advisor.model.SearchResult>> = _popularMovies

    private val _popularSeries = MutableLiveData<List<com.example.content_advisor.model.SearchResult>>()
    val popularSeries: LiveData<List<com.example.content_advisor.model.SearchResult>> = _popularSeries

    private val _trendingSeries = MutableLiveData<List<com.example.content_advisor.model.SearchResult>>()
    val trendingSeries: LiveData<List<com.example.content_advisor.model.SearchResult>> = _trendingSeries

    private val _trendingMovies = MutableLiveData<List<com.example.content_advisor.model.SearchResult>>()
    val trendingMovies: LiveData<List<com.example.content_advisor.model.SearchResult>> = _trendingMovies

    private val _personalizedMovieRecommendations = MutableLiveData<List<com.example.content_advisor.model.SearchResult>>()
    val personalizedMovieRecommendations: LiveData<List<com.example.content_advisor.model.SearchResult>> = _personalizedMovieRecommendations
    
    private val _personalizedSeriesRecommendations = MutableLiveData<List<com.example.content_advisor.model.SearchResult>>()
    val personalizedSeriesRecommendations: LiveData<List<com.example.content_advisor.model.SearchResult>> = _personalizedSeriesRecommendations


    private val _selectedMovie = MutableLiveData<Movie>()
    val selectedMovie: LiveData<Movie> = _selectedMovie
    
    private val _selectedTVSeries = MutableLiveData<TVSeries>()
    val selectedTVSeries: LiveData<TVSeries> = _selectedTVSeries
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val _movieCredits = MutableLiveData<Credits>()
    val movieCredits: LiveData<Credits> = _movieCredits

    private val _tvSeriesCredits = MutableLiveData<Credits>()
    val tvSeriesCredits: LiveData<Credits> = _tvSeriesCredits
    
    fun searchMulti(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("DEBUG: Searching for: $query")
                
                val response = repository.searchMulti(query)
                println("DEBUG: API Response received: ${response.results.size} results")
                _searchResults.value = response.results
                _error.value = null
            } catch (e: Exception) {
                println("DEBUG: Exception details: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _error.value = "Error occurred during search: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPopularContent() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val moviesResponse = repository.getPopularMovies()
                val seriesResponse = repository.getPopularTVSeries()
                val movies = moviesResponse.results
                    .map { it.toSearchResult() }
                    .take(20)
                val series = seriesResponse.results
                    .map { it.toSearchResult() }
                    .take(20)
                _popularMovies.value = movies
                _popularSeries.value = series
                _error.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error loading popular content: ${e.message}"
                _popularMovies.value = emptyList()
                _popularSeries.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Film detayları
    fun getMovieDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val movie = repository.getMovieDetails(movieId)
                _selectedMovie.value = movie
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading movie details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Dizi detayları
    fun getTVSeriesDetails(tvId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val tvSeries = repository.getTVSeriesDetails(tvId)
                _selectedTVSeries.value = tvSeries
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading TV series details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Film cast ve crew bilgileri
    fun getMovieCredits(movieId: Int) {
        viewModelScope.launch {
            try {
                val credits = repository.getMovieCredits(movieId)
                _movieCredits.value = credits
                println("DEBUG: Movie credits loaded - Cast: ${credits.cast.size}, Crew: ${credits.crew.size}")
            } catch (e: Exception) {
                println("DEBUG: Error loading movie credits: ${e.message}")
                _error.value = "Error loading cast information: ${e.message}"
            }
        }
    }
    
    // Dizi cast ve crew bilgileri
    fun getTVSeriesCredits(tvId: Int) {
        viewModelScope.launch {
            try {
                val credits = repository.getTVSeriesCredits(tvId)
                _tvSeriesCredits.value = credits
                println("DEBUG: TV credits loaded - Cast: ${credits.cast.size}, Crew: ${credits.crew.size}")
            } catch (e: Exception) {
                println("DEBUG: Error loading TV credits: ${e.message}")
                _error.value = "Error loading cast information: ${e.message}"
            }
        }
    }

    // Kullanıcının izlediği içeriklere göre PERSONALIZED öneriler getir
    fun getPersonalizedRecommendations(watchedContentData: List<Pair<Int, String>>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("DEBUG: Getting personalized recommendations for ${watchedContentData.size} watched items")

                val allMovieRecommendations = mutableSetOf<SearchResult>()
                val allSeriesRecommendations = mutableSetOf<SearchResult>()

                // Kullanıcının izlediği içerikler varsa personalized recommendations yap
                if (watchedContentData.isNotEmpty()) {
                    // Son izlenen film ve dizileri bul
                    val lastWatchedMovies = watchedContentData
                        .filter { it.second == "movie" }
                        .takeLast(3) // Son 3 izlenen film

                    val lastWatchedSeries = watchedContentData
                        .filter { it.second == "tv" }
                        .takeLast(3) // Son 3 izlenen dizi

                    println("DEBUG: Last watched movies: ${lastWatchedMovies.map { it.first }}")
                    println("DEBUG: Last watched series: ${lastWatchedSeries.map { it.first }}")

                    // Son izlenen filmler için recommendations al
                    for ((movieId, _) in lastWatchedMovies) {
                        try {
                            val movieRecommendations = repository.getRecommendedMovies(movieId)
                            val filteredResults = movieRecommendations.results
                                .filter { it.vote_average > 6.0 } // Kalite threshold
                                .take(8) // Her filmden 8 öneri
                                .map { it.toSearchResult() }

                            allMovieRecommendations.addAll(filteredResults)
                            println("DEBUG: Added ${filteredResults.size} movie recommendations from movie ID: $movieId")
                        } catch (e: Exception) {
                            println("DEBUG: Failed to get recommendations for movie ID $movieId: ${e.message}")
                        }
                    }

                    // Son izlenen diziler için recommendations al
                    for ((seriesId, _) in lastWatchedSeries) {
                        try {
                            val seriesRecommendations = repository.getRecommendedTVSeries(seriesId)
                            val filteredResults = seriesRecommendations.results
                                .filter { it.vote_average > 6.0 } // Kalite threshold
                                .take(8) // Her diziden 8 öneri
                                .map { it.toSearchResult() }

                            allSeriesRecommendations.addAll(filteredResults)
                            println("DEBUG: Added ${filteredResults.size} series recommendations from series ID: $seriesId")
                        } catch (e: Exception) {
                            println("DEBUG: Failed to get recommendations for series ID $seriesId: ${e.message}")
                        }
                    }
                }

                // İzlenen içeriklerden çıkar (duplicate engelleme)
                val watchedIds = watchedContentData.map { it.first }.toSet()
                val filteredMovieRecommendations = allMovieRecommendations
                    .filter { it.id !in watchedIds }
                    .sortedByDescending { it.voteAverage }
                    .distinctBy { it.id }
                    .take(20)

                val filteredSeriesRecommendations = allSeriesRecommendations
                    .filter { it.id !in watchedIds }
                    .sortedByDescending { it.voteAverage }
                    .distinctBy { it.id }
                    .take(20)

                // Personalized recommendations'ı set et
                _personalizedMovieRecommendations.value = filteredMovieRecommendations
                _personalizedSeriesRecommendations.value = filteredSeriesRecommendations

                println("DEBUG: Final personalized recommendations - Movies: ${filteredMovieRecommendations.size}, Series: ${filteredSeriesRecommendations.size}")
                _error.value = null

            } catch (e: Exception) {
                println("DEBUG: Error getting personalized recommendations: ${e.message}")
                e.printStackTrace()
                _error.value = "Error loading personalized recommendations: ${e.message}"
                _personalizedMovieRecommendations.value = emptyList()
                _personalizedSeriesRecommendations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Trending content'i güncelle (MainPageActivity'den çağrılır)
    fun updateTrendingMovies(trendingList: List<SearchResult>) {
        _trendingMovies.value = trendingList
        println("DEBUG: Trending movies updated with ${trendingList.size} items")
    }

    fun updateTrendingSeries(trendingList: List<SearchResult>) {
        _trendingSeries.value = trendingList
        println("DEBUG: Trending series updated with ${trendingList.size} items")
    }
} 