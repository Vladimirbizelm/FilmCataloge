package com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists

import com.example.filmcataloge.netConfiguration.popularMovies.Movie

data class GetCustomListDetails(
    val created_by: String,
    val description: String,
    val favorite_count: Int,
    val id: Int,
    val iso_639_1: String,
    val item_count: Int,
    val items: List<Movie>,
    val name: String,
    val page: Int,
    val poster_path: String,
    val total_pages: Int,
    val total_results: Int
)