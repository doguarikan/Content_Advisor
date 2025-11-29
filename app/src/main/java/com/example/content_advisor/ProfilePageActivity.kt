package com.example.content_advisor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.content_advisor.adapter.SearchAdapter
import com.example.content_advisor.adapter.PosterAdapter
import com.example.content_advisor.databinding.ActivityProfilePageBinding
import com.example.content_advisor.model.SearchResult
import com.example.content_advisor.viewmodel.MovieViewModel
import com.google.firebase.database.*
import android.widget.Toast

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var watchedMoviesAdapter: PosterAdapter<SearchResult>
    private lateinit var firebaseRef: DatabaseReference
    private val PREF_NAME = "MyAppPrefs"
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseRef = FirebaseDatabase.getInstance().getReference("users")
        getCurrentUserFromPrefs()
        
        setupViewModel()
        setupSearchRecyclerView()
        setupWatchedMoviesRecyclerView()
        setupClickListeners()
        setupSearchFunctionality()
        
        loadUserProfile()
    }

    private fun getCurrentUserFromPrefs() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        currentUserEmail = sharedPref.getString("email", null)
    }

    private fun loadUserProfile() {
        currentUserEmail?.let { email ->
            firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user?.mail == email) {
                            // Kullanıcı adını güncelle
                            binding.userNameText.text = user.name ?: "User"
                            
                            // İzlenen içerik sayısını güncelle
                            val watchedCount = user.watched?.size ?: 0
                            binding.moviesCountText.text = "$watchedCount Content Watched"
                            
                            // İzlenen filmleri yükle
                            loadWatchedMovies(user.watched ?: arrayListOf())
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfilePageActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadWatchedMovies(watchedList: ArrayList<String>) {
        val watchedMovies = mutableListOf<SearchResult>()
        
        if (watchedList.isEmpty()) {
            watchedMoviesAdapter.updateMovies(watchedMovies)
            return
        }
        
        // Her izlenen film için verileri parse et
        watchedList.forEach { movieData ->
            try {
                // movieData formatı: "id:title:posterPath:rating:overview:year:contentType" şeklinde
                val parts = movieData.split(":")
                if (parts.size >= 3) {
                    val movieId = parts[0].toIntOrNull() ?: return@forEach
                    val title = parts[1]
                    val posterPath = if (parts.size > 2 && parts[2].isNotEmpty()) parts[2] else null
                    val rating = if (parts.size > 3) parts[3].toDoubleOrNull() ?: 0.0 else 0.0
                    val overview = if (parts.size > 4) parts[4].replace(";", ":") else ""
                    val year = if (parts.size > 5) parts[5] else ""
                    val contentType = if (parts.size > 6) parts[6] else "movie"
                    
                    // SearchResult objesi oluştur
                    val searchResult = SearchResult(
                        id = movieId,
                        title = if (contentType == "movie") title else null,
                        name = if (contentType == "tv") title else null,
                        overview = overview,
                        posterPath = posterPath,
                        backdropPath = null,
                        voteAverage = rating,
                        voteCount = 0,
                        popularity = 0.0,
                        releaseDate = if (contentType == "movie") year else "",
                        firstAirDate = if (contentType == "tv") year else "",
                        mediaType = contentType
                    )
                    watchedMovies.add(searchResult)
                } else {
                    // Eski format için backward compatibility
                    val movieId = parts[0].toIntOrNull() ?: return@forEach
                    val title = parts[1]
                    val posterPath = if (parts.size > 2 && parts[2].isNotEmpty()) parts[2] else null
                    
                    val searchResult = SearchResult(
                        id = movieId,
                        title = title,
                        name = null,
                        overview = "",
                        posterPath = posterPath,
                        backdropPath = null,
                        voteAverage = 0.0,
                        voteCount = 0,
                        popularity = 0.0,
                        releaseDate = "",
                        firstAirDate = "",
                        mediaType = "movie"
                    )
                    watchedMovies.add(searchResult)
                }
            } catch (e: Exception) {
                println("Error parsing watched movie data: $movieData - ${e.message}")
            }
        }
        
        watchedMoviesAdapter.updateMovies(watchedMovies)
    }

    private fun setupWatchedMoviesRecyclerView() {
        watchedMoviesAdapter = PosterAdapter<SearchResult>(mutableListOf()) { searchResult ->
            // İzlenen filme tıklandığında detay sayfasına git
            val contentType = if (searchResult.isMovie()) "movie" else "tv"
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("content_type", contentType)
                putExtra("content_id", searchResult.id)
                putExtra("content_title", searchResult.getDisplayTitle())
                putExtra("content_year", searchResult.getYear())
                putExtra("content_rating", searchResult.voteAverage)
                putExtra("content_overview", searchResult.overview)
                putExtra("content_poster", searchResult.posterPath)
            }
            startActivity(intent)
        }
        
        binding.watchedMoviesRecyclerview.apply {
            layoutManager = GridLayoutManager(this@ProfilePageActivity, 2)
            adapter = watchedMoviesAdapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        
        // Arama sonuçları observer
        viewModel.searchResults.observe(this) { results ->
            println("DEBUG: Search results received: ${results.size} items")
            results.forEach { result ->
                println("DEBUG: ${result.getDisplayTitle()} - ${result.mediaType}")
            }
            searchAdapter.updateResults(results)
            // Sonuçlar varsa RecyclerView'ı göster
            binding.searchResultsRecyclerview.visibility = 
                if (results.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            println("DEBUG: Loading state: $isLoading")
            // Loading göstergesi eklenebilir
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                println("DEBUG: Error occurred: $it")
                // Hata mesajı gösterilebilir
            }
        }
    }

    private fun setupSearchRecyclerView() {
        searchAdapter = SearchAdapter { searchResult ->
            // Kullanıcı bir öğe seçtiğinde
            val contentType = if (searchResult.isMovie()) "movie" else "tv"
            println("DEBUG: SearchResult clicked - Title: '${searchResult.getDisplayTitle()}', ID: ${searchResult.id}, Type: '$contentType'")
            
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("content_type", contentType)
                putExtra("content_id", searchResult.id)
                putExtra("content_title", searchResult.getDisplayTitle())
                putExtra("content_year", searchResult.getYear())
                putExtra("content_rating", searchResult.voteAverage)
                putExtra("content_overview", searchResult.overview)
                putExtra("content_poster", searchResult.posterPath)
            }
            
            println("DEBUG: Starting DetailActivity with all SearchResult data and ID: ${searchResult.id}")
            startActivity(intent)
        }
        
        binding.searchResultsRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ProfilePageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = searchAdapter
        }
    }

    private fun setupClickListeners() {
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        binding.navChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        
        // Logout butonu için click listener ekle
        binding.logout.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        // SharedPreferences'ı temizle
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        
        // Login sayfasına yönlendir
        val intent = Intent(this, LoginPageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        // Sayfa her görüntülendiğinde kullanıcı profilini yenile
        loadUserProfile()
    }

    private fun setupSearchFunctionality() {
        // Debug: API key'i kontrol et
        println("DEBUG: BuildConfig.TMDB_API_KEY: ${com.example.content_advisor.BuildConfig.TMDB_API_KEY}")
        println("DEBUG: BuildConfig.OPENAI_API_KEY: ${com.example.content_advisor.BuildConfig.OPENAI_API_KEY}")
        println("DEBUG: TMDB API Key length: ${com.example.content_advisor.BuildConfig.TMDB_API_KEY.length}")
        println("DEBUG: OpenAI API Key length: ${com.example.content_advisor.BuildConfig.OPENAI_API_KEY.length}")
        
        // SearchView'da arama yapıldığında
        binding.movieSearchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        println("DEBUG: Search submitted: $it")
                        viewModel.searchMulti(it)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 2) { // En az 2 karakter yazıldığında arama yap
                        println("DEBUG: Search text changed: $it")
                        viewModel.searchMulti(it)
                    } else {
                        binding.searchResultsRecyclerview.visibility = android.view.View.GONE
                    }
                }
                return true
            }
        })
        
        // Arama butonuna tıklandığında
        binding.searchButton.setOnClickListener {
            val query = binding.movieSearchView.query.toString().trim()
            if (query.isNotEmpty()) {
                println("DEBUG: Search button clicked: $query")
                viewModel.searchMulti(query)
            }
        }
        
        // SearchView'ı açık tut
        binding.movieSearchView.isIconifiedByDefault = false
        binding.movieSearchView.isQueryRefinementEnabled = true
    }
}