package com.example.filmcataloge.uiConfiguration.moviesAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.MovieItemBinding
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.utils.MovieUtils

const val BASE_URL_FOR_IMAGES = "https://image.tmdb.org/t/p/original/"

class NestedRVAdapter() : ListAdapter<Movie, NestedRVAdapter.ViewHolder>(MovieDiffCallback()) {

    private var movies: List<Movie> = ArrayList()
    private var listener: OnItemClickListener? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: MovieItemBinding = MovieItemBinding.bind(itemView)
        fun bind(movie: Movie, listener: OnItemClickListener?) = with(binding) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(150, 200)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
            Glide.with(cardOfFilm.context)
                .load(BASE_URL_FOR_IMAGES + movie.backdrop_path)
                .apply(requestOptions)
                .into(cardOfFilm)
            ratingOfFilm.text = movie.vote_average.toString()
            titleOfFilm.text = movie.title
            ratingOfFilm.setTextColor(MovieUtils.getRatingColor(
                ratingOfFilm.context,
                rating = movie.vote_average
            ))
            ratingOfFilm.setBackgroundColor(MovieUtils.getRatingBackgroundColor(
                ratingOfFilm.context,
                rating = movie.vote_average
            ))
            itemView.setOnClickListener {
                Log.d("NestedRVAdapter", "onItemClick: $movie")
                listener?.onItemClick(adapterPosition, movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie, listener)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    fun setMovies(movies: List<Movie>) {
        this.movies = movies
        notifyItemRangeChanged(0, movies.size)
    }

    fun setListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, movie: Movie)
    }
}