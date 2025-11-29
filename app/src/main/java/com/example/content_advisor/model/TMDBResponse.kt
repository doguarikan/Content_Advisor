package com.example.content_advisor.model

import com.google.gson.annotations.SerializedName

data class TMDBResponse<T>(
    val results: List<T>,
    val page: Int,
    @SerializedName("total_pages")
    val total_pages: Int,
    @SerializedName("total_results")
    val total_results: Int
) 