package com.example.filmcataloge.uiConfiguration.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentFavoritesBinding
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.MainRVAdapter
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import kotlinx.coroutines.launch


class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private val categories = listOf("Favorite", "Watch later")
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var filmDetailsViewModel: FilmDetailsViewModel
    private lateinit var collectionsRepository: CollectionsRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(requireContext())
        filmDetailsViewModel =
            ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        collectionsRepository =
            CollectionsRepository(RetrofitClient.api, dataStoreManager, requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = setUpAdapter()

        binding.mainRV.visibility = View.GONE
        binding.emptyStateView.visibility = View.GONE

        loadCollections(adapter)
        observeCollectionUpdates(adapter)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }

    private fun loadCollections(adapter: MainRVAdapter) {
        lifecycleScope.launch {
            try {
                binding.mainRV.visibility = View.GONE
                binding.emptyStateView.visibility = View.GONE

                val sessionId = dataStoreManager.getSessionId()
                if (sessionId == null) {
                    showEmptyState(getString(R.string.favorite_fragment_register_to_get_access))
                    return@launch
                }

                val collectionsData = collectionsRepository.loadCollections()
                val hasFavorites = !collectionsData.favorites.isNullOrEmpty()
                val hasWatchlist = !collectionsData.watchlist.isNullOrEmpty()

                if (!hasFavorites && !hasWatchlist) {
                    showEmptyState(getString(R.string.collections_are_empty))
                    return@launch
                }

                collectionsData.favorites?.let { adapter.setMovies("Favorite", it) }
                collectionsData.watchlist?.let { adapter.setMovies("Watch later", it) }

                binding.emptyStateView.visibility = View.GONE
                binding.mainRV.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error loading collections", e)
                showEmptyState(getString(R.string.error_in_loading_collections))
            }
        }
    }

    private fun showEmptyState(message: String) {
        binding.mainRV.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
        binding.emptyStateMessage.text = message
    }

    private fun observeCollectionUpdates(adapter: MainRVAdapter) {
        filmDetailsViewModel.favoriteMoviesUpdated.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { updated ->
                if (updated) {
                    loadCollections(adapter)
                }
            }
        }

        filmDetailsViewModel.watchLaterMoviesUpdated.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { updated ->
                if (updated) {
                    loadCollections(adapter)
                }
            }
        }

        filmDetailsViewModel.sessionIdUpdated.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { updated ->
                if (updated) {
                    loadCollections(adapter)
                }
            }
        }

        lifecycleScope.launch {
            if (dataStoreManager.getSessionId() != null){
                loadCollections(adapter)
            }
        }
    }

    private fun setUpAdapter(): MainRVAdapter {
        binding.mainRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = MainRVAdapter(categories)
        binding.mainRV.adapter = adapter

        adapter.setListener(object : MainRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, category: String) {
                Toast.makeText(requireContext(), "category", Toast.LENGTH_SHORT).show()
            }
        })

        adapter.setNestedItemClickListener(object : NestedRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, movie: Movie) {
                filmDetailsViewModel.previousFragment.value = "favorites"
                (activity as MainActivity).showFilmDetailsFragment(movie.id)
            }
        })
        return adapter
    }
}