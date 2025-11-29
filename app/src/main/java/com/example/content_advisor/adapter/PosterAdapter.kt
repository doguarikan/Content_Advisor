package com.example.content_advisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.content_advisor.Content
import com.example.content_advisor.Movie
import com.example.content_advisor.TVSeries
import com.example.content_advisor.R
import com.example.content_advisor.model.SearchResult

// Poster gösterimi için generic interface
interface PosterItem {
    fun getTitle(): String
    fun getRating(): Double
    fun getPosterUrl(): String
}

// Content sınıfı için extension
fun Content.toPosterItem(): PosterItem = object : PosterItem {
    override fun getTitle(): String = this@toPosterItem.title
    override fun getRating(): Double = this@toPosterItem.vote_average
    override fun getPosterUrl(): String = this@toPosterItem.getPosterUrl()
}

// SearchResult sınıfı için extension
fun SearchResult.toPosterItem(): PosterItem = object : PosterItem {
    override fun getTitle(): String = this@toPosterItem.getDisplayTitle()
    override fun getRating(): Double = this@toPosterItem.voteAverage
    override fun getPosterUrl(): String = this@toPosterItem.getPosterUrl()
}

class PosterAdapter<T>(
    private var itemList: List<T> = emptyList(),
    private val onItemClick: (T) -> Unit
) : RecyclerView.Adapter<PosterAdapter.PosterViewHolder>() {

    class PosterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterImage: ImageView = view.findViewById(R.id.poster_image)
        val titleText: TextView = view.findViewById(R.id.poster_title)
        val ratingText: TextView = view.findViewById(R.id.poster_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poster, parent, false)
        return PosterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
        val item = itemList[position]
        
        val posterItem = when (item) {
            is Content -> item.toPosterItem()
            is SearchResult -> item.toPosterItem()
            else -> return
        }
        
        holder.titleText.text = posterItem.getTitle()
        holder.ratingText.text = "★ ${String.format("%.1f", posterItem.getRating())}"
        
        // Poster yükleme
        try {
            Glide.with(holder.posterImage.context)
                .load(posterItem.getPosterUrl())
                .placeholder(R.drawable.poster_background)
                .into(holder.posterImage)
        } catch (e: Exception) {
            holder.posterImage.setImageResource(R.drawable.poster_background)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = itemList.size

    // Content listesi için backward compatibility
    fun updateContent(newContent: List<Content>) {
        itemList = newContent as List<T>
        notifyDataSetChanged()
    }
    
    // SearchResult listesi için yeni method
    fun updateMovies(newMovies: List<SearchResult>) {
        itemList = newMovies as List<T>
        notifyDataSetChanged()
    }
    
    // Generic update method
    fun updateItems(newItems: List<T>) {
        itemList = newItems
        notifyDataSetChanged()
    }
} 