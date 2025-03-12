package com.example.filmcataloge.uiConfiguration.moviesAdapter

import androidx.recyclerview.widget.DiffUtil
import com.example.filmcataloge.netConfiguration.popularMovies.Movie

class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie) =
        oldItem == newItem
}