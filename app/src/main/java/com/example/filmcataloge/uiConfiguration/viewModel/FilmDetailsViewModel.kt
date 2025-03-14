package com.example.filmcataloge.uiConfiguration.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.filmcataloge.utils.Event

class FilmDetailsViewModel : ViewModel() {
    val previousFragment = MutableLiveData<String>()

    private val _favoriteMoviesUpdated = MutableLiveData<Event<Boolean>>()
    val favoriteMoviesUpdated: LiveData<Event<Boolean>> = _favoriteMoviesUpdated

    private val _watchLaterMoviesUpdated = MutableLiveData<Event<Boolean>>()
    val watchLaterMoviesUpdated: LiveData<Event<Boolean>> = _watchLaterMoviesUpdated

    fun notifyFavoriteMoviesUpdated() {
        _favoriteMoviesUpdated.value = Event(true)
        _watchLaterMoviesUpdated.value = Event(true)
    }



}