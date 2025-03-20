package com.example.filmcataloge.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.filmcataloge.R
import com.example.filmcataloge.netConfiguration.movieDetais.Genre
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import kotlin.math.roundToInt

object MovieUtils {
    fun formatRating(rating: Double): Double {
        return (rating * 10).roundToInt() / 10.0
    }

    fun formatDuration(duration: Int): String {
        val hours = duration / 60
        val minutes = duration % 60
        return "${hours}h ${minutes}m"
    }

    fun formatMoviesList(movies: List<Movie>): List<Movie> {
        return movies.map {
            it.apply { vote_average = formatRating(vote_average) }
        }
    }
    fun formatMovieDate(movie: List<Movie>): List<Movie>{
        return movie.map {
            it.apply { release_date = it.release_date.substring(0,4) }
        }
    }

    fun getRatingColor(context: Context, rating: Double): Int {
        return when (rating) {
            in 0.0..3.0 -> ContextCompat.getColor(context, R.color.rating_under_3_color)
            in 3.1..5.0 -> ContextCompat.getColor(context, R.color.rating_3_to_5_color)
            in 5.1..7.0 -> ContextCompat.getColor(context, R.color.rating_5_to_7_color)
            in 7.1..10.0 -> ContextCompat.getColor(context, R.color.rating_above_7)
            else -> ContextCompat.getColor(context, R.color.rating_under_3_color)
        }
    }
    fun getRatingBackgroundColor(context: Context, rating: Double): Int {
        return when (rating) {
            in 0.0..3.0 -> ContextCompat.getColor(context, R.color.rating_background_under_3_color)
            in 3.1..5.0 -> ContextCompat.getColor(context, R.color.rating_background_3_to_5_color)
            in 5.1..7.0 -> ContextCompat.getColor(context, R.color.rating_background_5_to_7_color)
            in 7.1..10.0 -> ContextCompat.getColor(context, R.color.rating_background_above_7)
            else -> ContextCompat.getColor(context, R.color.rating_background_under_3_color)
        }
    }
    fun formatGenres(genres: List<Genre>): String {
        return genres.take(2).joinToString (", ") { it.name }
    }
}