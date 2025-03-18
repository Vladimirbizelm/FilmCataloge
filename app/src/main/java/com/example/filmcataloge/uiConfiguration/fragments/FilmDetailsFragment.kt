package com.example.filmcataloge.uiConfiguration.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentFilmDetailsFragmentBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.movieDetais.MovieDetails
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.netConfiguration.reviewDetails.ReviewDetails
import com.example.filmcataloge.uiConfiguration.moviesAdapter.BASE_URL_FOR_IMAGES
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import com.example.filmcataloge.uiConfiguration.reviewsAdapter.ReviewsAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import com.example.filmcataloge.utils.MovieUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: (set up RV), (adapter for reviews), actors, (recommended films )
// TODO: add some images for films https://developer.themoviedb.org/reference/movie-images

class FilmDetailsFragment : Fragment() {

    lateinit var binding: FragmentFilmDetailsFragmentBinding
    private var filmID: Int? = null
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var collectionsRepository: CollectionsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filmID = arguments?.getInt("filmID")
        viewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        dataStoreManager = DataStoreManager(requireContext())
        collectionsRepository =
            CollectionsRepository(RetrofitClient.api, dataStoreManager, requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backFromFilmDetails.setOnClickListener {
            (activity as MainActivity).hideFilmDetailsFragment(viewModel.previousFragment.value.toString())
        }

        binding.addToFavoriteButton.setOnClickListener {
            handleCollectionButton(CollectionsRepository.CollectionType.FAVORITE)
        }

        binding.addToWatchlistButton.setOnClickListener {
            handleCollectionButton(CollectionsRepository.CollectionType.WATCHLIST)
        }

        binding.shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "https://www.themoviedb.org/movie/$filmID")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.moreOptions.setOnClickListener {

            val moreOptionsFragment = MoreOptionsButtonFragment.newInstance(filmID!!)
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager

            switchMainInterfaceCondition(false)
            fragmentManager.beginTransaction()
                .add(R.id.moreOptionsFragmentHolder, moreOptionsFragment)
                .addToBackStack(null).commit()
        }

        viewModel.moreOptionsFragmentClosed.observe(
            viewLifecycleOwner, {
                switchMainInterfaceCondition(true)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).hideFilmDetailsFragment(viewModel.previousFragment.value.toString())
    }

    private fun switchMainInterfaceCondition(isEnabled: Boolean) {


        binding.touchBlockerOverlay.visibility = if (isEnabled) View.GONE else View.VISIBLE

        binding.scrollView.isClickable = isEnabled
        binding.scrollView.isEnabled = isEnabled
        binding.mainFilmDetailsLayout.isClickable = isEnabled
        binding.mainFilmDetailsLayout.isEnabled = isEnabled

        binding.addToFavoriteButton.isEnabled = isEnabled
        binding.addToWatchlistButton.isEnabled = isEnabled
        binding.shareButton.isEnabled = isEnabled
        binding.moreOptions.isEnabled = isEnabled
        binding.backFromFilmDetails.isEnabled = isEnabled

        binding.moreOptionsFragmentHolder.visibility = if (isEnabled) View.GONE else View.VISIBLE
    }


    private fun handleCollectionButton(type: CollectionsRepository.CollectionType) {
        lifecycleScope.launch {
            try {
                val collections = collectionsRepository.loadCollections()
                val isInCollection = when (type) {
                    CollectionsRepository.CollectionType.FAVORITE -> collectionsRepository.isMovieInCollection(
                        collections.favorites, filmID ?: 0
                    )

                    CollectionsRepository.CollectionType.WATCHLIST -> collectionsRepository.isMovieInCollection(
                        collections.watchlist, filmID ?: 0
                    )
                }

                val success = when (type) {
                    CollectionsRepository.CollectionType.FAVORITE -> collectionsRepository.toggleFavorite(
                        filmID ?: 0,
                        isInCollection
                    )

                    CollectionsRepository.CollectionType.WATCHLIST -> collectionsRepository.toggleWatchlist(
                        filmID ?: 0,
                        isInCollection
                    )
                }

                if (success) {
                    updateButtonUI(type, !isInCollection)
                    when (type) {
                        CollectionsRepository.CollectionType.FAVORITE -> viewModel.notifyFavoriteMoviesUpdated()
                        CollectionsRepository.CollectionType.WATCHLIST -> viewModel.notifyWatchLaterMoviesUpdated()
                    }
                    val action = if (!isInCollection) "added to" else "removed from"
                    val typeName =
                        if (type == CollectionsRepository.CollectionType.FAVORITE) "favorite" else "watchlist"
                    Toast.makeText(
                        requireContext(), "Successfully $action $typeName", Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("FilmDetailsFragment", "Error handling collection button", e)
                Toast.makeText(requireContext(), "Error updating collection", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilmDetailsFragmentBinding.inflate(inflater, container, false)
        restoreOrCreateFragmentView(inflater, container, RetrofitClient.api)
        return binding.root
    }


    private fun restoreOrCreateFragmentView(
        inflater: LayoutInflater, container: ViewGroup?, api: API
    ) {
        binding = FragmentFilmDetailsFragmentBinding.inflate(inflater, container, false)
        val recommendedFilmsAdapter = NestedRVAdapter()
        binding.reviewsRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val reviewsAdapter = ReviewsAdapter()
        binding.reviewsRV.adapter = reviewsAdapter

        binding.recommendedFilmsRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendedFilmsRV.adapter = recommendedFilmsAdapter

        filmID?.let { id ->
            lifecycleScope.launch {
                val reviews = getMovieReviews(id, api)
                reviews?.let {
                    reviewsAdapter.setReviews(it.results)
                }
            }
            lifecycleScope.launch {
                val recommendedMovies = getMovieRecommendations(id, api)
                recommendedMovies?.let {
                    recommendedFilmsAdapter.setMovies(it)
                }
            }
            lifecycleScope.launch {
                val movieDetails = fetchMovieDetails(id, api)
                movieDetails?.let { updateUI(it) }

            }
        }

        updateCollectionsButtonsUI()

        recommendedFilmsAdapter.setListener(object : NestedRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, movie: Movie) {
                viewModel.previousFragment.value = "favorites"
                (activity as MainActivity).showFilmDetailsFragment(movie.id)
            }
        })

    }

    private fun updateCollectionsButtonsUI() {
        lifecycleScope.launch {
            try {
                val collections = collectionsRepository.loadCollections()
                val inFavorites =
                    collectionsRepository.isMovieInCollection(collections.favorites, filmID ?: 0)
                val inWatchList =
                    collectionsRepository.isMovieInCollection(collections.watchlist, filmID ?: 0)

                updateButtonUI(CollectionsRepository.CollectionType.FAVORITE, inFavorites)
                updateButtonUI(CollectionsRepository.CollectionType.WATCHLIST, inWatchList)
            } catch (e: Exception) {
                Log.e("FilmDetailsFragment", "Error updating collection buttons", e)
            }
        }
    }

    private fun updateButtonUI(
        type: CollectionsRepository.CollectionType, isInCollection: Boolean
    ) {
        when (type) {
            CollectionsRepository.CollectionType.FAVORITE -> {
                binding.addToFavoriteButton.setImageResource(
                    if (isInCollection) R.drawable.added_to_fav else R.drawable.favorite
                )
            }

            CollectionsRepository.CollectionType.WATCHLIST -> {
                binding.addToWatchlistButton.setImageResource(
                    if (isInCollection) R.drawable.save_add_added else R.drawable.save_add
                )
            }
        }
    }

    private suspend fun getMovieRecommendations(movieId: Int, api: API): List<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                val recommendedFilms = api.getRecommendedMoviesForThisMovieByID(movieId).results
                MovieUtils.formatMoviesList(recommendedFilms)
            } catch (e: Exception) {
                Log.e("film_details_fragment", "Error fetching movie recommendations", e)
                null
            }
        }
    }

    private suspend fun getMovieReviews(movieId: Int, api: API): ReviewDetails? {
        return withContext(Dispatchers.IO) {
            try {
                api.getMovieReviews(movieId)
            } catch (e: Exception) {
                Log.e("film_details_fragment", "Error fetching movie reviews", e)
                null
            }
        }
    }

    private suspend fun fetchMovieDetails(movieId: Int, api: API): MovieDetails? {
        return withContext(Dispatchers.IO) {
            try {
                api.getMovieDetails(movieId)
            } catch (e: Exception) {
                Log.e("film_details_fragment", "Error fetching movie details", e)
                null
            }
        }
    }

    private fun updateUI(movieDetails: MovieDetails) {
        binding.apply {
            val requestOptions =
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(300, 450)
                    .placeholder(R.drawable.loading).error(R.drawable.loading)
            Glide.with(this@FilmDetailsFragment)
                .load(BASE_URL_FOR_IMAGES + movieDetails.poster_path).apply(requestOptions)
                .apply(requestOptions).into(filmPoster)
            filmName.text = movieDetails.title

            val rating = MovieUtils.formatRating(movieDetails.vote_average)

            ratingOfFilm.text = rating.toString()
            ratingOfFilm.setTextColor(MovieUtils.getRatingBackgroundColor(requireContext(), rating))

            val date = movieDetails.release_date.split("-")
            val year = date[0]

            val genres = MovieUtils.formatGenres(movieDetails.genres)

            val duration = MovieUtils.formatDuration(movieDetails.runtime)

            val mainDescription =
                ("${year}, $genres \n ${movieDetails.production_countries[0].name} ${duration}${if (movieDetails.adult) ", 18+" else ""}")

            binding.reviewsCounter.text = movieDetails.vote_count.toString()
            filmMainDetails.text = mainDescription
            description.text = movieDetails.overview

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(filmID: Int): FilmDetailsFragment {
            val fragment = FilmDetailsFragment()
            val args = Bundle()
            args.putInt("filmID", filmID)
            fragment.arguments = args
            return fragment
        }
    }

}