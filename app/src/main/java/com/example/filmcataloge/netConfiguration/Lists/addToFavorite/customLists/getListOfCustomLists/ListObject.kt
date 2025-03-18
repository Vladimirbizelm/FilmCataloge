package com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.getListOfCustomLists

data class ListObject (
    val description: String,
    val favoriteCount: Int,
    val id: Int,
    val itemCount: Int,
    val iso_639_1: String,
    val list_type: String,
    val name: String,
    val poster_path: String
)