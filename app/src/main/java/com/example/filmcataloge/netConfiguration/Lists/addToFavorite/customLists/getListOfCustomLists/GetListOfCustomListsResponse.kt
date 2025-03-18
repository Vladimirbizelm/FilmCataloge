package com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.getListOfCustomLists

data class GetListOfCustomListsResponse (
    val page: Int,
    val results: List<ListObject>,
    val total_pages: Int,
    val total_results: Int
)