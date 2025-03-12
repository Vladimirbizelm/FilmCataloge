package com.example.filmcataloge.netConfiguration.reviewDetails

data class ReviewDetails(
    val id: Int,
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
)