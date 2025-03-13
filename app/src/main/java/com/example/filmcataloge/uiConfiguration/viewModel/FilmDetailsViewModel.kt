package com.example.filmcataloge.uiConfiguration.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.filmcataloge.utils.Event

class FilmDetailsViewModel : ViewModel() {
    val previousFragment = MutableLiveData<String>()

    private val _favoriteMoviesUpdated = MutableLiveData<Event<Boolean>>()
    val favoriteMoviesUpdated: LiveData<Event<Boolean>> = _favoriteMoviesUpdated

    fun notifyFavoriteMoviesUpdated() {
        _favoriteMoviesUpdated.value = Event(true)
    }
}