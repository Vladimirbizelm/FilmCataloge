package com.example.filmcataloge.netConfiguration.Lists.addToFavorite

data class AddToFavoriteRequest(
    val media_type: String,
    val media_id: Int,
    val favorite: Boolean
)