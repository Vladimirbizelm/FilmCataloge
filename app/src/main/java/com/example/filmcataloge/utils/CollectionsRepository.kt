package com.example.filmcataloge.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToFavorite.AddToFavoriteRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToWatchList.AddToWatchListRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.createCustomList.CreateCustomListRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.getListOfCustomLists.ListObject
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private const val API_KEY = Constants.API_KEY

class CollectionsRepository(
    private val api: API,
    private val dataStoreManager: DataStoreManager,
    private val context: Context
) {
    private val accountId = 21858168

    suspend fun getFavoriteMovies(): List<Movie>? {
        return handleApiCall("getFavoriteMovies") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall null
            val result = api.getFavoriteMovies(accountId, API_KEY, sessionId).results
            MovieUtils.formatMoviesList(result)
        }
    }

    suspend fun getWatchlistMovies(): List<Movie>? {
        return handleApiCall("getWatchlistMovies") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall null
            val result = api.getWatchList(accountId, API_KEY, sessionId).results
            MovieUtils.formatMoviesList(result)
        }
    }

    suspend fun getCustomLists(): List<ListObject> {
        return handleApiCall("getCustomLists") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall emptyList()

            val response = api.getCustomLists(
                accountId = accountId,
                page = 1,
                api_key = API_KEY,
                sessionId = sessionId
            )
            Log.d("CollectionsRepository", "getCustomLists response: $response")
            response.results
        } ?: emptyList()
    }

    suspend fun createCustomList(name: String, description: String): Boolean {
        return handleApiCall("createCustomList") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall false

            val response = api.createCustomList(
                api_key = API_KEY,
                sessionId = sessionId,
                createListRequest = CreateCustomListRequest(
                    name = name,
                    description = description,
                    language = "en-US"
                )
            )
            Log.d("CollectionsRepository", "List created: ${response.success}")
            response.success
        } ?: false
    }
    suspend fun addMovieToCustomList(listId: Int, movieId: Int): Boolean {
        return handleApiCall("addMovieToCustomList") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall false

            val response = api.addMovieToList(
                listId = listId,
                api_key = API_KEY,
                sessionId = sessionId,
                mediaId = movieId
            )
            Log.d("CollectionsRepository", "Movie added to list: ${response.status_code}")
            true
        } ?: false
    }

    suspend fun removeMovieFromCustomList(listId: Int, movieId: Int): Boolean{
        return handleApiCall("removeMovieFromCustomList") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall false

            val response = api.removeMovieFromList(
                listId = listId,
                api_key = API_KEY,
                sessionId = sessionId,
                mediaId = movieId
            )
            Log.d("CollectionsRepository", "Movie removed from list: ${response.status_code}")
            true
        } ?: false
    }

    suspend fun findCustomListByName(name: String): ListObject? {
        val lists = getCustomLists()
        return lists.find { it.name == name }
    }

    suspend fun createListIfNotExists(name: String, description: String): Boolean {
        val existingList = findCustomListByName(name)
        if (existingList == null) {
            return createCustomList(name, description)
        }
        return true
    }

    suspend fun isMovieInCustomList(listId: Int, movieId: Int): Boolean {
        return handleApiCall("isMovieInCustomList") {
            val sessionId = dataStoreManager.getSessionId() ?: return@handleApiCall false

            val response = api.checkMovieInList(
                listId = listId,
                api_key = API_KEY,
                movieId = movieId,
                sessionId = sessionId
            )
            Log.d("CollectionsRepository", "Movie in list: ${response.item_present}")
            response.item_present
        } ?: false
    }


    fun isMovieInCollection(collection: List<Movie>?, movieId: Int): Boolean {
        return collection?.any { it.id == movieId } ?: false
    }

    suspend fun toggleFavorite(movieId: Int, currentlyInCollection: Boolean): Boolean {
        return manageCollection(movieId, currentlyInCollection, CollectionType.FAVORITE)
    }

    suspend fun toggleWatchlist(movieId: Int, currentlyInCollection: Boolean): Boolean {
        return manageCollection(movieId, currentlyInCollection, CollectionType.WATCHLIST)
    }

    suspend fun loadCollections(): CollectionsData = coroutineScope {
        val favoriteDeferred = async { getFavoriteMovies() }
        val watchlistDeferred = async { getWatchlistMovies() }
        CollectionsData(
            favorites = favoriteDeferred.await(),
            watchlist = watchlistDeferred.await()
        )
    }

    private suspend fun manageCollection(
        movieId: Int,
        isInCollection: Boolean,
        collectionType: CollectionType
    ): Boolean {
        val sessionId = dataStoreManager.getSessionId() ?: run {
            Toast.makeText(context, "Session ID not found", Toast.LENGTH_SHORT).show()
            return false
        }

        return handleApiCall("manageCollection-$collectionType") {
            when (collectionType) {
                CollectionType.FAVORITE -> {
                    val request = AddToFavoriteRequest(
                        media_type = "movie",
                        media_id = movieId,
                        favorite = !isInCollection
                    )
                    api.addToFavorite(accountId, API_KEY, sessionId, request).success
                }

                CollectionType.WATCHLIST -> {
                    val request = AddToWatchListRequest(
                        media_type = "movie",
                        media_id = movieId,
                        watchlist = !isInCollection
                    )
                    api.addToWatchList(accountId, API_KEY, sessionId, request).success
                }
            }
        } ?: false
    }

    private suspend fun <T> handleApiCall(tag: String, block: suspend () -> T): T? {
        return withContext(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error in $tag: ${e.message}", e)
                null
            }
        }
    }

    enum class CollectionType { FAVORITE, WATCHLIST }

    data class CollectionsData(
        val favorites: List<Movie>?,
        val watchlist: List<Movie>?
    )
}