package com.example.filmcataloge.uiConfiguration.fragments

import android.content.Intent
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentFilmDetailsFragmentBinding
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.movieDetais.MovieDetails
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.BASE_URL_FOR_IMAGES
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import com.example.filmcataloge.uiConfiguration.reviewsAdapter.ReviewsAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import com.example.filmcataloge.utils.MovieUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmDetailsFragment : Fragment() {

    private lateinit var binding: FragmentFilmDetailsFragmentBinding
    private val viewModel by lazy { ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java] }
    private val dataStoreManager by lazy { DataStoreManager(requireContext()) }
    private val collectionsRepository by lazy {
        CollectionsRepository(RetrofitClient.api, dataStoreManager, requireContext())
    }
    private val api by lazy { RetrofitClient.api }

    private lateinit var movie: Movie
    private var filmID: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString("movie")?.let {
            movie = Gson().fromJson(it, Movie::class.java)
            filmID = movie.id
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilmDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
        setupRecyclerViews()
        loadMovieData()
        updateCollectionsButtonsUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        val previousFragment = viewModel.previousFragment.value.toString()
        Log.d("FilmDetailsFragment", "onDestroy $previousFragment")
        (activity as? MainActivity)?.hideFilmDetailsFragment(previousFragment)
    }

    private fun setupObservers() {
        viewModel.moreOptionsFragmentClosed.observe(viewLifecycleOwner) {
            switchMainInterfaceCondition(true)
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            backFromFilmDetails.setOnClickListener {
                (activity as? MainActivity)?.hideFilmDetailsFragment(viewModel.previousFragment.value.toString())
            }

            addToFavoriteButton.setOnClickListener {
                handleCollectionButton(CollectionsRepository.CollectionType.FAVORITE)
            }

            addToWatchlistButton.setOnClickListener {
                handleCollectionButton(CollectionsRepository.CollectionType.WATCHLIST)
            }

            shareButton.setOnClickListener {
                shareMovie()
            }

            moreOptions.setOnClickListener {
                showMoreOptionsFragment()
            }
        }
    }

    private fun setupRecyclerViews() {
        val recommendedFilmsAdapter = NestedRVAdapter().apply {
            setListener(object : NestedRVAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, movie: Movie) {
                    viewModel.previousFragment.value = "favorites"
                    (activity as? MainActivity)?.showFilmDetailsFragment(movie)
                }
            })
        }
        binding.recommendedFilmsRV.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedFilmsAdapter
        }

        binding.reviewsRV.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ReviewsAdapter()
        }
    }

    private fun loadMovieData() {
        filmID?.let { id ->
            lifecycleScope.launch {
                getMovieReviews(id)?.let { reviews ->
                    (binding.reviewsRV.adapter as? ReviewsAdapter)?.setReviews(reviews.results)
                }

                getMovieRecommendations(id)?.let { movies ->
                    (binding.recommendedFilmsRV.adapter as? NestedRVAdapter)?.setMovies(movies)
                }

                fetchMovieDetails(id)?.let { details ->
                    updateUI(details)
                }
            }
        }
    }

    private fun shareMovie() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://www.themoviedb.org/movie/$filmID")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun showMoreOptionsFragment() {
        val moreOptionsFragment = MoreOptionsButtonFragment.newInstance(movie)
        switchMainInterfaceCondition(false)
        requireActivity().supportFragmentManager.beginTransaction()
            .add(R.id.moreOptionsFragmentHolder, moreOptionsFragment)
            .addToBackStack(null).commit()
    }

    private fun switchMainInterfaceCondition(isEnabled: Boolean) {
        with(binding) {
            touchBlockerOverlay.visibility = if (isEnabled) View.GONE else View.VISIBLE
            moreOptionsFragmentHolder.visibility = if (isEnabled) View.GONE else View.VISIBLE

            scrollView.apply {
                isClickable = isEnabled
                this.isEnabled = isEnabled
            }
            mainFilmDetailsLayout.apply {
                isClickable = isEnabled
                this.isEnabled = isEnabled
            }

            listOf(
                addToFavoriteButton,
                addToWatchlistButton,
                shareButton,
                moreOptions,
                backFromFilmDetails
            ).forEach { it.isEnabled = isEnabled }
        }
    }

    private fun handleCollectionButton(type: CollectionsRepository.CollectionType) {
        lifecycleScope.launch {
            try {
                val collections = collectionsRepository.loadCollections()
                val isInCollection = when (type) {
                    CollectionsRepository.CollectionType.FAVORITE ->
                        collectionsRepository.isMovieInCollection(
                            collections.favorites,
                            filmID ?: 0
                        )

                    CollectionsRepository.CollectionType.WATCHLIST ->
                        collectionsRepository.isMovieInCollection(
                            collections.watchlist,
                            filmID ?: 0
                        )
                }

                val success = when (type) {
                    CollectionsRepository.CollectionType.FAVORITE ->
                        collectionsRepository.toggleFavorite(filmID ?: 0, isInCollection)

                    CollectionsRepository.CollectionType.WATCHLIST ->
                        collectionsRepository.toggleWatchlist(filmID ?: 0, isInCollection)
                }

                if (success) {
                    updateButtonUI(type, !isInCollection)

                    when (type) {
                        CollectionsRepository.CollectionType.FAVORITE -> viewModel.notifyFavoriteMoviesUpdated()
                        CollectionsRepository.CollectionType.WATCHLIST -> viewModel.notifyWatchLaterMoviesUpdated()
                    }

                    val action = if (!isInCollection) getString(R.string.added_to) else getString(R.string.deleted_from)
                    val typeName = if (type == CollectionsRepository.CollectionType.FAVORITE)
                        getString(R.string.favorites) else "${R.string.watch_later}"
                    Toast.makeText(
                        requireContext(), "${getString(R.string.successfully)} $action $typeName", Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("FilmDetailsFragment", getString(R.string.error_loading_collections), e)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_loading_collections),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateCollectionsButtonsUI() {
        lifecycleScope.launch {
            try {
                val collections = collectionsRepository.loadCollections()
                updateButtonUI(
                    CollectionsRepository.CollectionType.FAVORITE,
                    collectionsRepository.isMovieInCollection(collections.favorites, filmID ?: 0)
                )
                updateButtonUI(
                    CollectionsRepository.CollectionType.WATCHLIST,
                    collectionsRepository.isMovieInCollection(collections.watchlist, filmID ?: 0)
                )
            } catch (e: Exception) {
                Log.e("FilmDetailsFragment", "error loading buttons ui", e)
            }
        }
    }

    private fun updateButtonUI(
        type: CollectionsRepository.CollectionType,
        isInCollection: Boolean
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

    private suspend fun getMovieRecommendations(movieId: Int) = withContext(Dispatchers.IO) {
        try {
            val recommendedFilms = api.getRecommendedMoviesForThisMovieByID(movieId).results
            MovieUtils.formatMoviesList(recommendedFilms)
        } catch (e: Exception) {
            Log.e("FilmDetailsFragment", getString(R.string.error_loading_recommendations), e)
            null
        }
    }

    private suspend fun getMovieReviews(movieId: Int) = withContext(Dispatchers.IO) {
        try {
            api.getMovieReviews(movieId)
        } catch (e: Exception) {
            Log.e("FilmDetailsFragment", getString(R.string.error_loading_reviews), e)
            null
        }
    }

    private suspend fun fetchMovieDetails(movieId: Int) = withContext(Dispatchers.IO) {
        try {
            api.getMovieDetails(movieId)
        } catch (e: Exception) {
            Log.e("FilmDetailsFragment", getString(R.string.error_loading_film_details), e)
            null
        }
    }

    private fun updateUI(movieDetails: MovieDetails) {
        with(binding) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, 450)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)

            Glide.with(requireContext())
                .load(BASE_URL_FOR_IMAGES + movieDetails.poster_path)
                .apply(requestOptions)
                .into(filmPoster)

            filmName.text = movieDetails.title

            val rating = MovieUtils.formatRating(movieDetails.vote_average)
            ratingOfFilm.text = rating.toString()
            ratingOfFilm.setTextColor(MovieUtils.getRatingBackgroundColor(requireContext(), rating))

            val year = movieDetails.release_date.split("-")[0]
            val genres = MovieUtils.formatGenres(movieDetails.genres)
            val duration = MovieUtils.formatDuration(movieDetails.runtime)
            val ageRestriction = if (movieDetails.adult) ", 18+" else ""

            val countryName = if (movieDetails.production_countries.isNotEmpty())
                movieDetails.production_countries[0].name else ""

            val mainDetailsString = "$year, $genres \n$countryName $duration$ageRestriction"
            filmMainDetails.text = mainDetailsString
            reviewsCounter.text = movieDetails.vote_count.toString()
            description.text = movieDetails.overview
        }
    }



    companion object {
        @JvmStatic
        fun newInstance(movie: Movie) = FilmDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("movie", Gson().toJson(movie))
            }
        }
    }
}