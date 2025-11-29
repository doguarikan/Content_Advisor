package com.example.content_advisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.content_advisor.R
import com.example.content_advisor.model.SearchResult

class SearchAdapter(
    private var searchResults: List<SearchResult> = emptyList(),
    private val onItemClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterImage: ImageView = view.findViewById(R.id.movie_poster)
        val titleText: TextView = view.findViewById(R.id.movie_title)
        val yearText: TextView = view.findViewById(R.id.movie_year)
        val ratingText: TextView = view.findViewById(R.id.movie_rating)
        val typeText: TextView = view.findViewById(R.id.content_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val result = searchResults[position]
        
        holder.titleText.text = result.getDisplayTitle()
        holder.yearText.text = result.getYear()
        holder.ratingText.text = "★ ${result.voteAverage}"
        
        // İçerik tipini göster
        holder.typeText.text = if (result.isMovie()) "Film" else "Dizi"
        holder.typeText.setTextColor(
            if (result.isMovie()) 
                holder.itemView.context.getColor(R.color.main_green)
            else 
                holder.itemView.context.getColor(R.color.text_secondary)
        )
        
        // Poster yükleme
        if (result.posterPath != null) {
            Glide.with(holder.posterImage.context)
                .load(result.getPosterUrl())
                .placeholder(R.drawable.poster_background)
                .into(holder.posterImage)
        } else {
            holder.posterImage.setImageResource(R.drawable.poster_background)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(result)
        }
    }

    override fun getItemCount() = searchResults.size

    fun updateResults(newResults: List<SearchResult>) {
        println("DEBUG: SearchAdapter updating with ${newResults.size} results")
        searchResults = newResults
        notifyDataSetChanged()
    }
} 