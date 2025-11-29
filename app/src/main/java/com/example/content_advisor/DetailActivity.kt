package com.example.content_advisor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.content_advisor.databinding.ActivityDetailBinding
import com.example.content_advisor.model.SearchResult
import com.example.content_advisor.model.Credits
import com.example.content_advisor.viewmodel.MovieViewModel
import com.google.firebase.database.*
import android.widget.Toast

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var firebaseRef: DatabaseReference
    private val PREF_NAME = "MyAppPrefs"
    private var currentUserEmail: String? = null
    private var currentContentData: ContentData? = null

    data class ContentData(
        val id: Int,
        val title: String,
        val posterPath: String?,
        val contentType: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseRef = FirebaseDatabase.getInstance().getReference("users")
        getCurrentUserFromPrefs()

        setupViewModel()
        setupClickListeners()
        loadContentFromIntent()
    }
    
    private fun getCurrentUserFromPrefs() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        currentUserEmail = sharedPref.getString("email", null)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        
        // Film detayları observer - ek bilgiler için
        viewModel.selectedMovie.observe(this) { movie ->
            movie?.let {
                updateWithMovieDetails(it)
            }
        }
        
        // Dizi detayları observer - ek bilgiler için  
        viewModel.selectedTVSeries.observe(this) { tvSeries ->
            tvSeries?.let {
                updateWithTVSeriesDetails(it)
            }
        }
        
        // Movie credits observer
        viewModel.movieCredits.observe(this) { credits ->
            credits?.let {
                updateWithMovieCredits(it)
            }
        }
        
        // TV Series credits observer
        viewModel.tvSeriesCredits.observe(this) { credits ->
            credits?.let {
                updateWithTVCredits(it)
            }
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                println("DEBUG: Error loading additional details: $it")
            }
        }
    }

    private fun setupClickListeners() {
        binding.watchedButton.setOnClickListener {
            addToWatchedList()
        }
    }
    
    private fun addToWatchedList() {
        currentUserEmail?.let { email ->
            currentContentData?.let { content ->
                firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            if (user?.mail == email) {
                                // Mevcut izlenen listesini al
                                val watchedList = user.watched?.toMutableList() ?: mutableListOf()
                                
                                // İçerik bilgisini daha detaylı string olarak formatla
                                // Format: "id:title:posterPath:rating:overview:year:contentType"
                                val contentRating = intent.getDoubleExtra("content_rating", 0.0)
                                val contentOverview = intent.getStringExtra("content_overview") ?: ""
                                val contentYear = intent.getStringExtra("content_year") ?: ""
                                
                                val contentString = "${content.id}:${content.title}:${content.posterPath ?: ""}:${contentRating}:${contentOverview.replace(":", ";")}:${contentYear}:${content.contentType}"
                                
                                // Eğer zaten eklenmemişse ekle
                                if (!watchedList.any { it.startsWith("${content.id}:") }) {
                                    watchedList.add(contentString)
                                    
                                    // Firebase'de güncelle
                                    val updatedUser = user.copy(watched = ArrayList(watchedList))
                                    userSnapshot.ref.setValue(updatedUser)
                                        .addOnSuccessListener {
                                            binding.watchedButton.text = "Added to Watched!"
                                            binding.watchedButton.isEnabled = false
                                            Toast.makeText(this@DetailActivity, "Added to your watched list!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { 
                                            Toast.makeText(this@DetailActivity, "Error adding to watched list", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    binding.watchedButton.text = "Already Watched"
                                    binding.watchedButton.isEnabled = false
                                    Toast.makeText(this@DetailActivity, "Already in your watched list!", Toast.LENGTH_SHORT).show()
                                }
                                break
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } ?: run {
            Toast.makeText(this, "Please login to add to watched list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContentFromIntent() {
        val contentTitle = intent.getStringExtra("content_title") ?: ""
        val contentYear = intent.getStringExtra("content_year") ?: ""
        val contentRating = intent.getDoubleExtra("content_rating", 0.0)
        val contentOverview = intent.getStringExtra("content_overview") ?: ""
        val contentPoster = intent.getStringExtra("content_poster")
        val contentType = intent.getStringExtra("content_type") ?: ""
        val contentId = intent.getIntExtra("content_id", -1)
        
        println("DEBUG: Loading content from intent - Title: '$contentTitle', Type: '$contentType', ID: $contentId")
        
        // Current content data'yi set et
        if (contentId != -1) {
            currentContentData = ContentData(
                id = contentId,
                title = contentTitle,
                posterPath = contentPoster,
                contentType = contentType
            )
        }
        
        // SearchResult verilerini göster
        displayContentDetails(contentTitle, contentYear, contentRating, contentOverview, contentPoster, contentType)
        
        // İzlendi butonunun durumunu kontrol et
        checkWatchedStatus()
        
        // Ek detaylar için API çağrıları yap
        if (contentId != -1) {
            when (contentType) {
                "movie" -> {
                    println("DEBUG: Fetching movie details and credits for ID: $contentId")
                    viewModel.getMovieDetails(contentId)
                    viewModel.getMovieCredits(contentId)
                }
                "tv" -> {
                    println("DEBUG: Fetching TV details and credits for ID: $contentId")
                    viewModel.getTVSeriesDetails(contentId)
                    viewModel.getTVSeriesCredits(contentId)
                }
            }
        }
    }
    
    private fun checkWatchedStatus() {
        currentUserEmail?.let { email ->
            currentContentData?.let { content ->
                firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            if (user?.mail == email) {
                                val watchedList = user.watched ?: arrayListOf()
                                
                                // Bu içerik zaten izlendi mi kontrol et (sadece ID'yi kontrol et)
                                val isAlreadyWatched = watchedList.any { 
                                    it.startsWith("${content.id}:")
                                }
                                
                                if (isAlreadyWatched) {
                                    binding.watchedButton.text = "Already Watched"
                                    binding.watchedButton.isEnabled = false
                                } else {
                                    binding.watchedButton.text = "Mark as Watched"
                                    binding.watchedButton.isEnabled = true
                                }
                                break
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Hata durumunda sessizce devam et
                        println("DEBUG: Error checking watched status: ${error.message}")
                    }
                })
            }
        }
    }

    private fun displayContentDetails(
        title: String,
        year: String,
        rating: Double,
        overview: String,
        posterPath: String?,
        contentType: String
    ) {
        binding.movieTitle.text = title
        binding.movieYear.text = if (year.isNotEmpty()) "($year)" else ""
        binding.movieRating.text = "★ ${String.format("%.1f", rating)}"
        binding.movieOverview.text = overview.ifEmpty { "No overview available for this ${if (contentType == "movie") "movie" else "series"}." }
        
        // Şimdilik boş olan alanlar için varsayılan değerler
        binding.movieDuration.text = if (contentType == "movie") "Movie" else "TV Series"
        binding.movieDirector.text = if (contentType == "movie") "Director information not available" else "Creator information not available"
        binding.movieCast.text = "Cast information not available"
        
        // Poster yükleme
        if (!posterPath.isNullOrEmpty()) {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$posterPath")
                .placeholder(R.drawable.poster_background)
                .into(binding.moviePoster)
        } else {
            binding.moviePoster.setImageResource(R.drawable.poster_background)
        }
        
        println("DEBUG: Content details loaded - Title: '$title', Type: '$contentType'")
    }

    private fun updateWithMovieDetails(movie: Movie) {
        // Bu fonksiyon API'den ek detaylar geldiğinde çalışacak
        println("DEBUG: Additional movie details received")
    }

    private fun updateWithTVSeriesDetails(tvSeries: TVSeries) {
        // Bu fonksiyon API'den ek detaylar geldiğinde çalışacak
        println("DEBUG: Additional TV series details received")
    }
    
    private fun updateWithMovieCredits(credits: Credits) {
        // Director'ı crew'dan bul
        val director = credits.crew.find { it.job == "Director" }
        binding.movieDirector.text = director?.name ?: "Director information not available"
        
        // İlk 3 oyuncuyu al
        val topCast = credits.cast.take(3).map { it.name }
        binding.movieCast.text = if (topCast.isNotEmpty()) {
            topCast.joinToString(", ")
        } else {
            "Cast information not available"
        }
        
        println("DEBUG: Movie credits updated - Director: ${director?.name}, Cast: ${topCast.size}")
    }
    
    private fun updateWithTVCredits(credits: Credits) {
        // Creator'ı crew'dan bul (TV için)
        val creator = credits.crew.find { it.job == "Creator" || it.job == "Executive Producer" }
        binding.movieDirector.text = creator?.name ?: "Creator information not available"
        
        // İlk 3 oyuncuyu al
        val topCast = credits.cast.take(3).map { it.name }
        binding.movieCast.text = if (topCast.isNotEmpty()) {
            topCast.joinToString(", ")
        } else {
            "Cast information not available"
        }
        
        println("DEBUG: TV credits updated - Creator: ${creator?.name}, Cast: ${topCast.size}")
    }
} 