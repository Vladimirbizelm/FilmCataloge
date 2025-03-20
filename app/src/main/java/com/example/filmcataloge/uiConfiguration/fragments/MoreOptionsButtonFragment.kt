package com.example.filmcataloge.uiConfiguration.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentMoreOptionsButtonLayoutBinding
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.BASE_URL_FOR_IMAGES
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MoreOptionsButtonFragment : Fragment() {
    private lateinit var binding: FragmentMoreOptionsButtonLayoutBinding
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var collectionsRepository: CollectionsRepository
    private var movie: Movie? = null
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        dataStoreManager = DataStoreManager(requireContext())
        collectionsRepository = CollectionsRepository(
            RetrofitClient.api,
            DataStoreManager(requireContext()),
            requireContext()
        )

        arguments?.getString("movie")?.let {
            movie = Gson().fromJson(it, Movie::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoreOptionsButtonLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        movie?.let {
            updateUI(it)
            checkWatchedStatus()
        } ?: run {
            requireActivity().supportFragmentManager.popBackStack()
            Toast.makeText(requireContext(), getString(R.string.error_loading_film_details), Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupClickListeners() {
        binding.closeButton.setOnClickListener {
            viewModel.notifyMoreOptionsFragmentClosed()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.addToListFilmButton.setOnClickListener {
            Toast.makeText(requireContext(), "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.addToWatchedButton.setOnClickListener {
            toggleWatchedStatus()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(movie: Movie) {
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(150, 200)
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)

        Glide.with(requireContext())
            .load(BASE_URL_FOR_IMAGES + movie.backdrop_path)
            .apply(requestOptions)
            .into(binding.filmPoster)

        binding.title.text = movie.title
        binding.originalTitleAndDate.text =
            "${movie.original_title} (${movie.release_date.split("-")[0]})"
    }

    private fun checkWatchedStatus() {
        val movieId = movie?.id ?: return

        lifecycleScope.launch {
            try {
                val success = collectionsRepository.createListIfNotExists(
                    "Watched",
                    "List of watched films"
                )

                if (success) {
                    val list = collectionsRepository.findCustomListByName("Watched")
                    list?.id?.let { listId ->
                        val isInWatched = collectionsRepository.isMovieInCustomList(listId, movieId)
                        updateWatchedButton(isInWatched)
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.error_while_checking_status)} ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun toggleWatchedStatus() {
        val movieId = movie?.id ?: return

        lifecycleScope.launch {
            val list = collectionsRepository.findCustomListByName("Watched")
            list?.id?.let { listId ->
                val isInWatched = collectionsRepository.isMovieInCustomList(listId, movieId)

                if (isInWatched) {
                    collectionsRepository.removeMovieFromCustomList(listId, movieId)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.deleted_from_viewed),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    collectionsRepository.addMovieToCustomList(listId, movieId)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.added_to_viewed),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                viewModel.historyUpdated()
                updateWatchedButton(!isInWatched)
            }
        }
    }

    private fun updateWatchedButton(isWatched: Boolean) {
        binding.addToWatchedButton.setImageResource(
            if (isWatched) R.drawable.added_to_watched else R.drawable.add_to_watched
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.notifyMoreOptionsFragmentClosed()
    }

    companion object {
        @JvmStatic
        fun newInstance(movie: Movie): MoreOptionsButtonFragment {
            return MoreOptionsButtonFragment().apply {
                arguments = Bundle().apply {
                    putString("movie", Gson().toJson(movie))
                }
            }
        }
    }
}