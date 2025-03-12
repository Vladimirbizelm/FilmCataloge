package com.example.filmcataloge.uiConfiguration.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.filmcataloge.netConfiguration.movieDetais.MovieDetails
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.netConfiguration.reviewDetails.Result
import com.example.filmcataloge.utils.Event

class FilmDetailsViewModel : ViewModel() {
    val movieDetails = MutableLiveData<MovieDetails>()
    val reviews = MutableLiveData<List<Result>>()
    val recommendedFilms = MutableLiveData<List<Movie>>()

    val popularMovies = MutableLiveData<List<Movie>>()
    val topRatedMovies = MutableLiveData<List<Movie>>()
    val favoriteMovies = MutableLiveData<List<Movie>>()
    val searchResults = MutableLiveData<List<Movie>>()

    // Одноразовое событие для обновления избранного
    private val _favoriteMoviesUpdated = MutableLiveData<Event<Boolean>>()
    val favoriteMoviesUpdated: LiveData<Event<Boolean>> = _favoriteMoviesUpdated

    fun notifyFavoriteMoviesUpdated() {
        _favoriteMoviesUpdated.value = Event(true)
    }
}