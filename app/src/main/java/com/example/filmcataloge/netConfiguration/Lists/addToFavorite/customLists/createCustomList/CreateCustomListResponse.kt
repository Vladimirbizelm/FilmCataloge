package com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.createCustomList

data class CreateCustomListResponse(
    val status_code: Int,
    val status_message: String,
    val success: Boolean,
    val list_id: Int
)