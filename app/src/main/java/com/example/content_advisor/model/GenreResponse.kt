package com.example.content_advisor.model

import com.google.gson.annotations.SerializedName

data class GenreResponse(
    val genres: List<Genre>
)

data class Genre(
    val id: Int,
    val name: String
) 