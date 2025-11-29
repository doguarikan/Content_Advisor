package com.example.content_advisor

data class User(
    val id : String? = null,
    val mail : String? = null,
    val name : String? = null,
    val pass : String? = null,
    val watched : ArrayList<String>? = null
)
