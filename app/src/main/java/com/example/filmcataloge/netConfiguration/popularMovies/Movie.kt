package com.example.filmcataloge.netConfiguration.popularMovies

data class Movie(
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: ArrayList<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    var release_date: String,
    val title: String,
    val video: Boolean,
    var vote_average: Double,
    val vote_count: Int
)