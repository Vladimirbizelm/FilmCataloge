package com.example.filmcataloge.uiConfiguration.moviesAdapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.SearchMovieItemBinding
import com.example.filmcataloge.netConfiguration.popularMovies.Movie

class SearchFilmsAdapter(): RecyclerView.Adapter<SearchFilmsAdapter.ViewHolder>() {

    private val movies: List<Movie> = ArrayList()
    private var listener: OnItemClickListener? = null

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding: SearchMovieItemBinding = SearchMovieItemBinding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(movie: Movie, listener: OnItemClickListener?) = with(binding){

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(150, 200)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)

            Glide.with(filmPoster.context)
                .load(BASE_URL_FOR_IMAGES + movie.backdrop_path)
                .apply(requestOptions)
                .into(filmPoster)

            filmTitle.text = movie.title
            originalTitleAndYear.text = movie.original_title + " " + movie.release_date
            ratingOfFilm.text = movie.vote_average.toString()

            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition, movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.search_movie_item, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie, listener)
    }
    fun setMovies(movies: List<Movie>){
        (this.movies as ArrayList).addAll(movies)
        notifyItemRangeChanged(0, movies.size)
    }

    fun setListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, movie: Movie)
    }
}