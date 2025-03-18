package com.example.filmcataloge.netConfiguration

import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToFavorite.AddToFavoriteRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToFavorite.AddToFavoriteResponse
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToWatchList.AddToWatchListRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.basicLists.addToWatchList.AddToWatchListResponse
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.AddRemoveMovieToListResponse
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.CheckMovieInCustomListResponse
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.createCustomList.CreateCustomListRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.createCustomList.CreateCustomListResponse
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.getListOfCustomLists.GetListOfCustomListsResponse
import com.example.filmcataloge.netConfiguration.createSession.LoginRequest
import com.example.filmcataloge.netConfiguration.createSession.RequestTokenResponse
import com.example.filmcataloge.netConfiguration.createSession.SessionRequest
import com.example.filmcataloge.netConfiguration.createSession.SessionResponse
import com.example.filmcataloge.netConfiguration.movieDetais.MovieDetails
import com.example.filmcataloge.netConfiguration.popularMovies.MoviesResponse
import com.example.filmcataloge.netConfiguration.recommendedFilms.RecommendedFilms
import com.example.filmcataloge.netConfiguration.reviewDetails.ReviewDetails
import com.example.filmcataloge.utils.Constants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

const val API_KEY = Constants.API_KEY

interface API {
    //main page requests
    @GET("movie/popular?language=en-US&page=1")
    suspend fun getPopularMovies(@Query("api_key") api_key: String = API_KEY): MoviesResponse

    @GET("discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=vote_average.desc&without_genres=99,10755&vote_count.gte=200")
    suspend fun getTopRatedMovies(@Query("api_key") api_key: String = API_KEY): MoviesResponse

    // movie details requests
    @GET("movie/{movie_id}?language=en-US")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") api_key: String = API_KEY
    ): MovieDetails

    @GET("movie/{movie_id}/reviews?language=en-US&page=1")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("api_key") api_key: String = API_KEY
    ): ReviewDetails

    @GET("movie/{movie_id}/recommendations?language=en-US&page=1")
    suspend fun getRecommendedMoviesForThisMovieByID(
        @Path("movie_id") movieId: Int,
        @Query("api_key") api_key: String = API_KEY
    ): RecommendedFilms

    //search page requests
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("api_key") api_key: String = API_KEY
    ): MoviesResponse

    //for userData
    @GET("authentication/token/new")
    suspend fun getRequestToken(@Query("api_key") api_key: String = API_KEY): RequestTokenResponse

    @POST("authentication/token/validate_with_login")
    suspend fun validateLogin(
        @Query("api_key") api_key: String = API_KEY,
        @Body loginRequest: LoginRequest
    ): RequestTokenResponse

    @POST("authentication/session/new")
    suspend fun createSession(
        @Query("api_key") apikey: String = API_KEY,
        @Body sessionRequest: SessionRequest
    ): SessionResponse

    //fav list
    @POST("account/{account_id}/favorite")
    suspend fun addToFavorite(
        @Path("account_id") accountId: Int,
        @Query("api_key") api_key: String = API_KEY,
        @Query("session_id") sessionId: String,
        @Body favoriteRequest: AddToFavoriteRequest
    ): AddToFavoriteResponse

    @GET("account/{account_id}/favorite/movies?language=en-US&page=1&sort_by=created_at.asc")
    suspend fun getFavoriteMovies(
        @Path("account_id") accountId: Int,
        @Query("api_key") api_key: String = API_KEY,
        @Query("session_id") sessionId: String
    ): MoviesResponse

    //watch later list
    @POST("account/{account_id}/watchlist")
    suspend fun addToWatchList(
        @Path("account_id") accountId: Int,
        @Query("api_key") api_key: String = API_KEY,
        @Query("session_id") sessionId: String,
        @Body watchListRequest: AddToWatchListRequest
    ) : AddToWatchListResponse

    @GET("account/{account_id}/watchlist/movies?language=en-US&page=1&sort_by=created_at.asc")
    suspend fun getWatchList(
        @Path("account_id") accountId: Int,
        @Query("api_key") api_key: String = API_KEY,
        @Query("session_id") sessionId: String
    ): MoviesResponse



    //watched list
    @POST("list")
    suspend fun createCustomList(
        @Query("api_key") api_key: String = API_KEY,
        @Query("session_id") sessionId: String,
        @Body createListRequest: CreateCustomListRequest
    ): CreateCustomListResponse

    //get custom lists
    @GET("account/{account_id}/lists")
    suspend fun getCustomLists(
        @Path("account_id") accountId: Int,
        @Query("page") page: Int,
        @Query("api_key") api_key: String = API_KEY,
        @Query("session_id") sessionId: String
    ): GetListOfCustomListsResponse

    //add movie to custom list
    @POST("list/{list_id}/add_item")
    suspend fun addMovieToList(
        @Path("list_id") listId: Int,
        @Query("api_key") api_key: String,
        @Query("session_id") sessionId: String,
        @Query("media_id") mediaId: Int
    ): AddRemoveMovieToListResponse

    //remove movie from custom list
    @POST("list/{list_id}/remove_item")
    suspend fun removeMovieFromList(
        @Path("list_id") listId: Int,
        @Query("api_key") api_key: String,
        @Query("session_id") sessionId: String,
        @Query("media_id") mediaId: Int
    ): AddRemoveMovieToListResponse

    //check movie in custom List
    @GET("list/{list_id}/item_status")
    suspend fun checkMovieInList(
        @Path("list_id") listId: Int,
        @Query("api_key") api_key: String,
        @Query("movie_id") movieId: Int,
        @Query("session_id") sessionId: String
    ): CheckMovieInCustomListResponse


}

