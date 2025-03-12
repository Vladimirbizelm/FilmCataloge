package com.example.filmcataloge.uiConfiguration.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.MainActivity

import com.example.filmcataloge.databinding.FragmentSearchBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.SearchFilmsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val baseURL = "https://api.themoviedb.org/3/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val adapter = setUpAdapter()

        val api = RetrofitClient.api
        // TODO: fix it
        binding.searchBar.setOnQueryTextListener( object : OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    getMoviesByTitle(query, adapter, api)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        }
        )

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() = SearchFragment()
    }

    private fun setUpAdapter(): SearchFilmsAdapter {
        binding.mainRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = SearchFilmsAdapter()
        binding.mainRV.adapter = adapter
        adapter.setListener(
            object : SearchFilmsAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, movie: Movie) {
                    (activity as MainActivity).showFilmDetailsFragment(movie.id)
                }
            }
        )
        return adapter
    }

    private fun getMoviesByTitle(title: String, adapter: SearchFilmsAdapter, api: API) {
        CoroutineScope(Dispatchers.IO).launch {
            val movies = async { api.searchMovies(title, "en-US", 1, API_KEY) }
            val moviesDeferred = movies.await()
            val listOfPopularMovies: ArrayList<Movie> = moviesDeferred.results
            listOfPopularMovies.forEach {
                it.vote_average = (it.vote_average * 10).roundToInt() / 10.0
            }
            withContext(Dispatchers.Main) {
                adapter.setMovies(listOfPopularMovies)
            }

        }
    }

}