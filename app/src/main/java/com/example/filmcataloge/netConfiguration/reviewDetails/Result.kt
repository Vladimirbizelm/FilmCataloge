package com.example.filmcataloge.netConfiguration.reviewDetails

data class Result(
    val author: String,
    val author_details: AuthorDetails,
    val content: String,
    val created_at: String,
    val id: String,
    val updated_at: String,
    val url: String
)