package com.example.filmcataloge.uiConfiguration.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.filmcataloge.utils.Event

class FilmDetailsViewModel : ViewModel() {
    val previousFragment = MutableLiveData<String>()

    private val _sessionId = MutableLiveData<Event<Boolean>>()
    val sessionIdUpdated: LiveData<Event<Boolean>> = _sessionId
    fun notifySessionIdUpdated() {
        _sessionId.value = Event(true)
    }

    private val _favoriteMoviesUpdated = MutableLiveData<Event<Boolean>>()
    val favoriteMoviesUpdated: LiveData<Event<Boolean>> = _favoriteMoviesUpdated
    fun notifyFavoriteMoviesUpdated() {
        _favoriteMoviesUpdated.value = Event(true)
    }


    private val _watchLaterMoviesUpdated = MutableLiveData<Event<Boolean>>()
    val watchLaterMoviesUpdated: LiveData<Event<Boolean>> = _watchLaterMoviesUpdated
    fun notifyWatchLaterMoviesUpdated() {
        _watchLaterMoviesUpdated.value = Event(true)
    }

    private val _moreOptionsFragmentClosed = MutableLiveData<Event<Boolean>>()
    val moreOptionsFragmentClosed: LiveData<Event<Boolean>> = _moreOptionsFragmentClosed
    fun notifyMoreOptionsFragmentClosed() {
        _moreOptionsFragmentClosed.value = Event(true)
    }

}