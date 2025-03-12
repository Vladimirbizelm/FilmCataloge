package com.example.filmcataloge.netConfiguration.recommendedFilms

import com.example.filmcataloge.netConfiguration.popularMovies.Movie

data class RecommendedFilms(
    val page: Int,
    val results: List<Movie>,
    val total_pages: Int,
    val total_results: Int
)