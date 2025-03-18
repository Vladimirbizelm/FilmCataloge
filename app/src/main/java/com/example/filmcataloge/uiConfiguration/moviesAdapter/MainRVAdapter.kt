package com.example.filmcataloge.uiConfiguration.moviesAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.NestedRvBinding
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.utils.MovieUtils

class MainRVAdapter(private val categories: List<String>) :
    RecyclerView.Adapter<MainRVAdapter.MainViewHolder>() {

    private val moviesMap: MutableMap<String, List<Movie>> = mutableMapOf()
    private var listener: OnItemClickListener? = null
    private var nestedItemClickListener: NestedRVAdapter.OnItemClickListener? = null

    class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: NestedRvBinding = NestedRvBinding.bind(view)

        fun bind(category: String, movies: List<Movie>, listener: OnItemClickListener?, nestedItemClickListener: NestedRVAdapter.OnItemClickListener?) = with(binding) {

            if (movies.isEmpty().not()){
                categoryName.text = category
            }

            nestedRv.layoutManager = LinearLayoutManager(nestedRv.context, LinearLayoutManager.HORIZONTAL, false)
            val nestedRVAdapter = NestedRVAdapter()
            nestedRv.adapter = nestedRVAdapter
            nestedRVAdapter.setMovies(movies)

            if (nestedItemClickListener != null) {
                nestedRVAdapter.setListener(nestedItemClickListener)
            }
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition, category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nested_rv, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val category = categories[position]
        val movies = moviesMap[category] ?: emptyList()
        holder.bind(category, movies, listener, nestedItemClickListener)
    }

    fun setMovies(category: String, movies: List<Movie>) {
        val changed = MovieUtils.formatMoviesList(movies)
        moviesMap[category] = changed
        notifyItemRangeChanged(0, movies.size)
    }

    fun setListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun setNestedItemClickListener(listener: NestedRVAdapter.OnItemClickListener) {
        this.nestedItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, category: String)
    }
}