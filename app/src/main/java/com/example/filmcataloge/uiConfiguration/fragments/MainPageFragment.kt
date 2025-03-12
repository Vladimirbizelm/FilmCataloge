package com.example.filmcataloge.uiConfiguration.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.databinding.FragmentMainPageBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.MainRVAdapter
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


class MainPageFragment : Fragment() {

    private val categories: ArrayList<String> = arrayListOf("Popular", "Top Rated", "Upcoming")
    private lateinit var binding: FragmentMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainPageBinding.inflate(inflater, container, false)
        val api = RetrofitClient.api
        val adapter = setUpAdapter()
        getMoviesForMainPage(api, adapter)

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainPageFragment()
    }

    private fun setUpAdapter(): MainRVAdapter {
        binding.mainRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = MainRVAdapter(categories).apply {
            setListener(object : MainRVAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, category: String) {
                    Toast.makeText(requireContext(), "category", Toast.LENGTH_SHORT).show()
                }
            })
            setNestedItemClickListener(object : NestedRVAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, movie: Movie) {
                    Log.d("MainActivity", "onItemNestedRVClick: $movie")
                    (activity as MainActivity).showFilmDetailsFragment(movie.id)
                }
            })
        }
        binding.mainRV.adapter = adapter
        return adapter
    }

    private fun getMoviesForMainPage(api: API, adapter: MainRVAdapter) {
        CoroutineScope(Dispatchers.IO).launch {
            val popularMoviesResponse = async { api.getPopularMovies(API_KEY) }
            val topRatedMovieResponse = async { api.getTopRatedMovies(API_KEY) }

            val popularMoviesDeferred = popularMoviesResponse.await()
            val topRatedMoviesDeferred = topRatedMovieResponse.await()

            val listOfPopularMovies: ArrayList<Movie> = popularMoviesDeferred.results
            val listOfTopRatedMovies: ArrayList<Movie> = topRatedMoviesDeferred.results

            listOfPopularMovies.forEach {
                it.vote_average = (it.vote_average * 10).roundToInt() / 10.0
            }
            listOfTopRatedMovies.forEach {
                it.vote_average = (it.vote_average * 10).roundToInt() / 10.0
            }

            withContext(Dispatchers.Main) {
                adapter.setMovies("Popular", listOfPopularMovies)
                adapter.setMovies("Top Rated", listOfTopRatedMovies)
            }

        }
    }
}