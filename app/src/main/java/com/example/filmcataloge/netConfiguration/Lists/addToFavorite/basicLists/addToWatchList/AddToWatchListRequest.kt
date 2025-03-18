package com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToWatchList

data class AddToWatchListRequest (
    val media_type: String,
    val media_id: Int,
    val watchlist: Boolean
)
