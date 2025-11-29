package com.example.content_advisor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.content_advisor.adapter.PosterAdapter
import com.example.content_advisor.databinding.ActivityMainPageBinding
import com.example.content_advisor.viewmodel.MovieViewModel
import com.example.content_advisor.model.SearchResult
import com.google.firebase.database.*
import android.widget.Toast

class MainPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPageBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var moviesAdapter: PosterAdapter<SearchResult>
    private lateinit var seriesAdapter: PosterAdapter<SearchResult>
    private lateinit var trending_movies_Adapter: PosterAdapter<SearchResult>
    private lateinit var trending_series_Adapter: PosterAdapter<SearchResult>
    private lateinit var firebaseRef: DatabaseReference
    private val PREF_NAME = "MyAppPrefs"
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseRef = FirebaseDatabase.getInstance().getReference("users")
        getCurrentUserFromPrefs()

        setupViewModel()
        setupRecyclerViews()
        setupClickListeners()
        loadData()
        loadPersonalizedRecommendations()
    }
    
    private fun getCurrentUserFromPrefs() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        currentUserEmail = sharedPref.getString("email", null)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        
        // Popular Movies observer'ını ekle
        viewModel.popularMovies.observe(this) { movies ->
            println("DEBUG: Popular movies loaded: ${movies.size} items")
            updateTrendingContent()
        }
        
        // Popular Series observer'ını ekle
        viewModel.popularSeries.observe(this) { series ->
            println("DEBUG: Popular series loaded: ${series.size} items")
            updateTrendingContent()
        }
        
        // Observer'ları ayarla
        viewModel.personalizedMovieRecommendations.observe(this) { movies ->
            moviesAdapter.updateMovies(movies)
            println("DEBUG: Personalized movie recommendations loaded: ${movies.size} items")
        }
        
        viewModel.personalizedSeriesRecommendations.observe(this) { series ->
            seriesAdapter.updateMovies(series)
            println("DEBUG: Personalized series recommendations loaded: ${series.size} items")
        }
        
        // This Week's Popular (sabit trending content)
        viewModel.trendingMovies.observe(this) { trending ->
            trending_movies_Adapter.updateMovies(trending)
            println("DEBUG: Trending content updated with ${trending.size} items")
        }

        viewModel.trendingSeries.observe(this) { trending ->
            trending_series_Adapter.updateMovies(trending)
            println("DEBUG: Trending content updated with ${trending.size} items")
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            // Loading göstergesi eklenebilir
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                // Hata mesajı gösterilebilir
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Popular movies ve series'i birleştirip trending content'e koy
    private fun updateTrendingContent() {
        val popularMovies = viewModel.popularMovies.value ?: emptyList()
        val popularSeries = viewModel.popularSeries.value ?: emptyList()
        
        // İki listeyi birleştir ve karıştır (mix movies and series)
        val moviesList = mutableListOf<SearchResult>()
        moviesList.addAll(popularMovies.take(10)) // İlk 10 popular movie
        val seriesList = mutableListOf<SearchResult>()
        seriesList.addAll(popularSeries.take(10)) // İlk 10 popular series

        // ViewModel'daki trendingContent'i güncelle
        viewModel.updateTrendingMovies(moviesList.take(15)) // En fazla 20 item
        viewModel.updateTrendingSeries(seriesList.take(15)) // En fazla 20 item
        
    }

    private fun setupRecyclerViews() {
        // Filmler için adapter
        moviesAdapter = PosterAdapter<SearchResult> { searchResult ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("content_type", "movie")
                putExtra("content_id", searchResult.id)
                putExtra("content_title", searchResult.getDisplayTitle())
                putExtra("content_year", searchResult.getYear())
                putExtra("content_rating", searchResult.voteAverage)
                putExtra("content_overview", searchResult.overview)
                putExtra("content_poster", searchResult.posterPath)
            }
            startActivity(intent)
        }
        
        // Diziler için adapter
        seriesAdapter = PosterAdapter<SearchResult> { searchResult ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("content_type", "tv")
                putExtra("content_id", searchResult.id)
                putExtra("content_title", searchResult.getDisplayTitle())
                putExtra("content_year", searchResult.getYear())
                putExtra("content_rating", searchResult.voteAverage)
                putExtra("content_overview", searchResult.overview)
                putExtra("content_poster", searchResult.posterPath)
            }
            startActivity(intent)
        }
        
        // Son haftaki popüler içerikler için adapter
        trending_movies_Adapter = PosterAdapter<SearchResult> { searchResult ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("content_type", if (searchResult.isMovie()) "movie" else "tv")
                putExtra("content_id", searchResult.id)
                putExtra("content_title", searchResult.getDisplayTitle())
                putExtra("content_year", searchResult.getYear())
                putExtra("content_rating", searchResult.voteAverage)
                putExtra("content_overview", searchResult.overview)
                putExtra("content_poster", searchResult.posterPath)
            }
            startActivity(intent)
        }
        trending_series_Adapter = PosterAdapter<SearchResult> { searchResult ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("content_type", if (searchResult.isMovie()) "movie" else "tv")
                putExtra("content_id", searchResult.id)
                putExtra("content_title", searchResult.getDisplayTitle())
                putExtra("content_year", searchResult.getYear())
                putExtra("content_rating", searchResult.voteAverage)
                putExtra("content_overview", searchResult.overview)
                putExtra("content_poster", searchResult.posterPath)
            }
            startActivity(intent)
        }
        
        // RecyclerView'ları ayarla
        binding.moviesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainPageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }
        
        binding.seriesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainPageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = seriesAdapter
        }

        binding.recommendedMoviesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainPageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = trending_movies_Adapter
        }

        binding.recommendedSeriesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainPageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = trending_series_Adapter
        }
    }

    private fun setupClickListeners() {
        binding.navChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        binding.navProfile.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadPersonalizedRecommendations()
    }

    private fun loadData() {
        viewModel.loadPopularContent()
    }

    private fun loadPersonalizedRecommendations() {
        currentUserEmail?.let { email ->
            firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user?.mail == email) {
                            val watchedList = user.watched ?: arrayListOf()

                            if (watchedList.isNotEmpty()) {
                                // Son izlenen içerikleri al
                                val recentWatched = watchedList.takeLast(5)
                                val watchedContentData = mutableListOf<Pair<Int, String>>()

                                recentWatched.forEach { watchedItem ->
                                    try {
                                        val parts = watchedItem.split(":")
                                        if (parts.size >= 7) {
                                            val contentId = parts[0].toIntOrNull()
                                            val contentType = parts[6]

                                            if (contentId != null) {
                                                watchedContentData.add(Pair(contentId, contentType))
                                                println("DEBUG: Added watched content - ID: $contentId, Type: $contentType")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        println("DEBUG: Error parsing watched item: $watchedItem - ${e.message}")
                                    }
                                }

                                // Son izlenen film ve diziyi bul
                                val lastMovie = watchedContentData.findLast { it.second == "movie" }
                                val lastSeries = watchedContentData.findLast { it.second == "tv" }

                                println("DEBUG: Last watched movie ID: ${lastMovie?.first}")
                                println("DEBUG: Last watched series ID: ${lastSeries?.first}")
                                println("DEBUG: Total watched content data: ${watchedContentData.size} items")

                                // Personalized recommendations'ı çağır
                                viewModel.getPersonalizedRecommendations(watchedContentData)

                            } else {
                                println("DEBUG: No watched content found for user")
                                // Kullanıcının izlediği içerik yoksa boş liste ile çağır
                                viewModel.getPersonalizedRecommendations(emptyList())
                            }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("DEBUG: Error loading watched content: ${error.message}")
                    // Hata durumunda boş liste ile çağır
                    viewModel.getPersonalizedRecommendations(emptyList())
                }
            })
        } ?: run {
            println("DEBUG: No current user email found")
            // Kullanıcı giriş yapmamışsa boş liste ile çağır
            viewModel.getPersonalizedRecommendations(emptyList())
        }
    }
}