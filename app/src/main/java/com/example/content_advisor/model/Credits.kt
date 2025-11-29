package com.example.content_advisor.model

import com.google.gson.annotations.SerializedName

data class Credits(
    val id: Int,
    val cast: List<CastMember>,
    val crew: List<CrewMember>
)

data class CastMember(
    val id: Int,
    val name: String,
    @SerializedName("original_name")
    val originalName: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?,
    val popularity: Double,
    val order: Int
)

data class CrewMember(
    val id: Int,
    val name: String,
    @SerializedName("original_name")
    val originalName: String,
    val job: String,
    val department: String,
    @SerializedName("profile_path")
    val profilePath: String?,
    val popularity: Double
) 