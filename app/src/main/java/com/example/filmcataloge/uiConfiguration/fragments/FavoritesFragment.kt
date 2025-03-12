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


// TODO: get lists from personal data of user 

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private val categories = listOf("Favorite movies", "Watch later")
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var filmDetailsViewModel: FilmDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(requireContext())
        filmDetailsViewModel = ViewModelProvider(requireActivity()).get(FilmDetailsViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val api = RetrofitClient.api
        val adapter = setUpAdapter()

        lifecycleScope.launch {
            val sessionId = dataStoreManager.getSessionId()
            val list = getFavoritePageMoviesCollections(api, sessionId)
            Log.d("FavoritesFragment", "onCreateView: $list")
            if (list != null) {
                Log.d("FavoritesFragment", "onCreateView: $list")
                adapter.setMovies("Favorite", list)
            }
        }
        filmDetailsViewModel.favoriteMoviesUpdated.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { updated ->
                if (updated) {
                    lifecycleScope.launch {
                        val sessionId = dataStoreManager.getSessionId()
                        Log.d("FavoritesFragment", "onViewCreated: $sessionId")
                        val list = getFavoritePageMoviesCollections(api, sessionId)
                        if (list != null) {
                            adapter.setMovies("Favorite movies", list)
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
                    Log.d("MainActivity", "onItemNestedRVClick: $movie")
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
                Log.d("FavoritesFragment", "getFavoritePageMoviesCollections: $a")
                a
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error fetching fav movies", e)
                null
            }
        }
    }
}