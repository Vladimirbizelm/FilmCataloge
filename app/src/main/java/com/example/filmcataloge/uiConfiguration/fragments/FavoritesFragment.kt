package com.example.filmcataloge.uiConfiguration.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.databinding.FragmentFavoritesBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.MainRVAdapter
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private val categories = listOf("Favorite", "Watch later")
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var filmDetailsViewModel: FilmDetailsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(requireContext())
        filmDetailsViewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val api = RetrofitClient.api
        val adapter = setUpAdapter()

        lifecycleScope.launch {
            val sessionId = dataStoreManager.getSessionId()
            val listOfFavoriteMovies = getFavoritePageMoviesCollections(api, sessionId)
            val listOfWatchLaterMovies = getWatchLaterMoviesCollection(api, sessionId)
            if (listOfFavoriteMovies != null && listOfWatchLaterMovies != null) {
                adapter.setMovies("Favorite", listOfFavoriteMovies)
                adapter.setMovies("Watch later", listOfWatchLaterMovies)
            }
        }

        filmDetailsViewModel.favoriteMoviesUpdated.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { updated ->
                if (updated) {
                    lifecycleScope.launch {
                        val sessionId = dataStoreManager.getSessionId()
                        val listOfFavoriteMovies = getFavoritePageMoviesCollections(api, sessionId)
                        val listOfWatchLaterMovies = getWatchLaterMoviesCollection(api, sessionId)
                        if (listOfFavoriteMovies != null && listOfWatchLaterMovies != null) {
                            adapter.setMovies("Favorite", listOfFavoriteMovies)
                            adapter.setMovies("Watch later", listOfWatchLaterMovies)
                        }
                    }
                }
            }
        }
        filmDetailsViewModel.watchLaterMoviesUpdated.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { updated ->
                if (updated) {
                    lifecycleScope.launch {
                        val sessionId = dataStoreManager.getSessionId()
                        val listOfFavoriteMovies = getFavoritePageMoviesCollections(api, sessionId)
                        val listOfWatchLaterMovies = getWatchLaterMoviesCollection(api, sessionId)
                        if (listOfFavoriteMovies != null && listOfWatchLaterMovies != null) {
                            adapter.setMovies("Favorite", listOfFavoriteMovies)
                            adapter.setMovies("Watch later", listOfWatchLaterMovies)
                        }
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }

    private fun setUpAdapter(): MainRVAdapter {
        binding.mainRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = MainRVAdapter(categories)
        binding.mainRV.adapter = adapter

        adapter.setListener(
            object : MainRVAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, category: String) {
                    Toast.makeText(requireContext(), "category", Toast.LENGTH_SHORT).show()
                }
            }
        )

        adapter.setNestedItemClickListener(
            object : NestedRVAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, movie: Movie) {
                    filmDetailsViewModel.previousFragment.value = "favorites"
                    (activity as MainActivity).showFilmDetailsFragment(movie.id)
                }
            }
        )
        return adapter
    }

    private suspend fun getFavoritePageMoviesCollections(api: API, sessionId: String?): List<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                val a = api.getFavoriteMovies(21858168, API_KEY, sessionId ?: "").results
                a.forEach {
                    it.vote_average = (it.vote_average * 10).toInt() / 10.0
                }
                Log.d("FavoritesFragment", "getFavoritePageMoviesCollections: $a")
                a
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error fetching fav movies", e)
                null
            }
        }
    }

    private suspend fun getWatchLaterMoviesCollection(api: API, sessionId: String?): List<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                val a = api.getWatchList(21858168, API_KEY, sessionId ?: "").results
                a.forEach {
                    it.vote_average = (it.vote_average * 10).toInt() / 10.0
                }
                Log.d("FavoritesFragment", "getFavoritePageMoviesCollections: $a")
                a
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error fetching fav movies", e)
                null
            }
        }
    }
}