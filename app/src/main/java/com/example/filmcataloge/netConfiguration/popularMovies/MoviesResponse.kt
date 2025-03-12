package com.example.filmcataloge.netConfiguration.popularMovies


data class MoviesResponse(
    val page: Int,
    val results: ArrayList<Movie>,
    val total_pages: Int,
    val total_results: Int,)