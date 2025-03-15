package com.example.filmcataloge.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.addToFavorite.AddToFavoriteRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.addToWatchList.AddToWatchListRequest
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

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