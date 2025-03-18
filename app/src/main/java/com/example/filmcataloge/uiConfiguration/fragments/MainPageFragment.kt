package com.example.filmcataloge.uiConfiguration.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.databinding.FragmentMainPageBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.MainRVAdapter
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import com.example.filmcataloge.utils.Constants
import com.example.filmcataloge.utils.MovieUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainPageFragment : Fragment() {

    private val categories: ArrayList<String> =
        arrayListOf("Popular", "Top Rated", "You wanted to watch")
    private lateinit var binding: FragmentMainPageBinding
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var collectionsRepository: CollectionsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        dataStoreManager = DataStoreManager(requireContext())
        collectionsRepository =
            CollectionsRepository(RetrofitClient.api, dataStoreManager, requireContext())
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        binding = FragmentMainPageBinding.inflate(inflater, container, false)
        val api = RetrofitClient.api
        val adapter = setUpAdapter()
        getMoviesForMainPage(api, adapter)
        Log.d("MainPageFragment", "onCreateView ${Constants.API_KEY}")

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
                    viewModel.previousFragment.value = "home"
                    (activity as MainActivity).showFilmDetailsFragment(movie.id)
                }
            })
        }
        binding.mainRV.adapter = adapter
        return adapter
    }

    private fun getMoviesForMainPage(api: API, adapter: MainRVAdapter) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val popularMoviesResponse = async { api.getPopularMovies(Constants.API_KEY) }
                val topRatedMovieResponse = async { api.getTopRatedMovies(Constants.API_KEY) }

                val popularMoviesDeferred = popularMoviesResponse.await()
                val topRatedMoviesDeferred = topRatedMovieResponse.await()

                val listOfPopularMovies = MovieUtils.formatMoviesList(popularMoviesDeferred.results)
                val listOfTopRatedMovies = MovieUtils.formatMoviesList(topRatedMoviesDeferred.results)

                listOfPopularMovies.forEach {
                    Log.d("MainPageFragment", "Popular movie: ${it.title}")
                }
                withContext(Dispatchers.Main) {
                    adapter.setMovies("Popular", listOfPopularMovies)
                    adapter.setMovies("Top Rated", listOfTopRatedMovies)
                }
            } catch (e: Exception) {
                Log.e("MainPageFragment", "Error loading movies", e)
            }

        }
    }
}