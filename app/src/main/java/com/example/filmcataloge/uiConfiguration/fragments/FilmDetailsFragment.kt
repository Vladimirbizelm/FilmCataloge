package com.example.filmcataloge.uiConfiguration.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentFilmDetailsFragmentBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.addToFavorite.AddToFavoriteRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.addToWatchList.AddToWatchListRequest
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.movieDetais.MovieDetails
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.netConfiguration.reviewDetails.ReviewDetails
import com.example.filmcataloge.uiConfiguration.moviesAdapter.BASE_URL_FOR_IMAGES
import com.example.filmcataloge.uiConfiguration.moviesAdapter.NestedRVAdapter
import com.example.filmcataloge.uiConfiguration.reviewsAdapter.ReviewsAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// TODO: (set up RV), (adapter for reviews), actors, (recommended films )
// TODO: add some images for films https://developer.themoviedb.org/reference/movie-images
// TODO: fix updateUI fun

class FilmDetailsFragment : Fragment() {

    private lateinit var binding: FragmentFilmDetailsFragmentBinding
    private var filmID: Int? = null
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filmID = arguments?.getInt("filmID")
        viewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        dataStoreManager = DataStoreManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backFromFilmDetails.setOnClickListener {
            (activity as MainActivity).hideFilmDetailsFragment(viewModel.previousFragment.value.toString())
        }

        binding.addToFavoriteButton.setOnClickListener {
            lifecycleScope.launch {
                val favCollection = getFavoritePageMoviesCollections(
                    RetrofitClient.api,
                    dataStoreManager.getSessionId()
                )
                Log.d("FilmDetailsFragment", "onViewCreated: $favCollection")
                if (dataStoreManager.getSessionId() != null && favCollection != null) {
                    filmID?.let {
                        addToFavorite(
                            it,
                            RetrofitClient.api,
                            isAddedToTheCollection(favCollection)
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Please authenticate", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.addToWatchlistButton.setOnClickListener {

            lifecycleScope.launch {
                val watchListCollection = getWatchListMoviesCollection(
                    RetrofitClient.api,
                    dataStoreManager.getSessionId()
                )
                Log.d("FilmDetailsFragment", "onViewCreated: $watchListCollection")
                if (dataStoreManager.getSessionId() != null && watchListCollection != null) {
                    filmID?.let {
                        addToWatchList(
                            it,
                            RetrofitClient.api,
                            isAddedToTheCollection(watchListCollection)
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Please authenticate", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }

    private fun isAddedToTheCollection(collection: ArrayList<Movie>): Boolean {
        for (i in collection) {
            if (i.id == filmID) {
                return true
            }
        }
        return false
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilmDetailsFragmentBinding.inflate(inflater, container, false)
        restoreOrCreateFragmentView(inflater, container, RetrofitClient.api)
        return binding.root
    }


    private fun restoreOrCreateFragmentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        api: API
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
            val favCollection = getFavoritePageMoviesCollections(
                RetrofitClient.api,
                dataStoreManager.getSessionId()
            )
            if (favCollection != null) {
                binding.addToFavoriteButton.setImageResource(
                    if (isAddedToTheCollection(favCollection)) R.drawable.added_to_fav else R.drawable.favorite
                )
            }

            val watchListCollection = getWatchListMoviesCollection(
                RetrofitClient.api,
                dataStoreManager.getSessionId()
            )
            if (watchListCollection != null) {
                binding.addToWatchlistButton.setImageResource(
                    if (isAddedToTheCollection(watchListCollection)) R.drawable.save_add_added else R.drawable.save_add
                )
            }
        }
    }

    private suspend fun getFavoritePageMoviesCollections(
        api: API,
        sessionId: String?
    ): ArrayList<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                api.getFavoriteMovies(21858168, API_KEY, sessionId ?: "").results
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error fetching movies", e)
                null
            }
        }
    }

    private suspend fun getWatchListMoviesCollection(
        api: API,
        sessionId: String?
    ): ArrayList<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                api.getWatchList(21858168, API_KEY, sessionId ?: "").results
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error fetching movies", e)
                null
            }
        }
    }

    private suspend fun addToFavorite(movieId: Int, api: API, isAddedToFavorite: Boolean) {
        val sessionId = dataStoreManager.getSessionId()
        if (sessionId != null) {
            if (isAddedToFavorite) {
                try {
                    binding.addToFavoriteButton.setImageResource(R.drawable.favorite)
                    val favoriteRequest = AddToFavoriteRequest(
                        media_type = "movie",
                        media_id = movieId,
                        favorite = false
                    )
                    val response = api.addToFavorite(
                        accountId = 21858168,
                        sessionId = sessionId,
                        favoriteRequest = favoriteRequest
                    )
                    if (response.success) {
                        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT)
                            .show()
                        viewModel.notifyFavoriteMoviesUpdated()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to add to favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("FilmDetailsFragment", "Error adding to favorites", e)
                    Toast.makeText(
                        requireContext(),
                        "Error adding to favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                try {
                    binding.addToFavoriteButton.setImageResource(R.drawable.added_to_fav)
                    val favoriteRequest = AddToFavoriteRequest(
                        media_type = "movie",
                        media_id = movieId,
                        favorite = true
                    )
                    val response = api.addToFavorite(
                        accountId = 21858168,
                        sessionId = sessionId,
                        favoriteRequest = favoriteRequest
                    )
                    if (response.success) {
                        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT)
                            .show()
                        viewModel.notifyFavoriteMoviesUpdated()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to add to favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("FilmDetailsFragment", "Error adding to favorites", e)
                    Toast.makeText(
                        requireContext(),
                        "Error adding to favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else {
            Toast.makeText(requireContext(), "Session ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun addToWatchList(movieId: Int, api: API, isAddedToWatchList: Boolean) {
        val sessionId = dataStoreManager.getSessionId()
        if (sessionId != null) {
            if (isAddedToWatchList) {
                try {
                    binding.addToWatchlistButton.setImageResource(R.drawable.save_add)
                    val watchListResponse = AddToWatchListRequest(
                        media_type = "movie",
                        media_id = movieId,
                        watchlist = false
                    )
                    val response = api.addToWatchList(
                        accountId = 21858168,
                        sessionId = sessionId,
                        watchListRequest = watchListResponse
                    )
                    Log.d("FilmDetailsFragment", "addToWatchList top: $response")
                    if (response.success) {
                        Toast.makeText(requireContext(), "Added to watchlist", Toast.LENGTH_SHORT).show()
                        viewModel.notifyFavoriteMoviesUpdated()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to add to watchlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("FilmDetailsFragment", "Error adding to watchlist", e)
                    Toast.makeText(
                        requireContext(),
                        "Error adding to watchlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                try {
                    binding.addToWatchlistButton.setImageResource(R.drawable.save_add_added)
                    val warchListRequest = AddToWatchListRequest(
                        media_type = "movie",
                        media_id = movieId,
                        watchlist = true
                    )
                    val response = api.addToWatchList(
                        accountId = 21858168,
                        sessionId = sessionId,
                        watchListRequest = warchListRequest
                    )
                    Log.d("FilmDetailsFragment", "addToWatchList: $response")
                    if (response.success) {
                        Toast.makeText(requireContext(), "Added to watchlist", Toast.LENGTH_SHORT)
                            .show()
                        viewModel.notifyFavoriteMoviesUpdated()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to add to watchlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("FilmDetailsFragment", "Error adding to watchlist", e)
                    Toast.makeText(
                        requireContext(),
                        "Error adding to watchlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else {
            Toast.makeText(requireContext(), "Session ID not found", Toast.LENGTH_SHORT).show()
        }

    }

    private suspend fun getMovieRecommendations(movieId: Int, api: API): List<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                val recommendedFilms = api.getRecommendedMoviesForThisMovieByID(movieId).results
                recommendedFilms.forEach {
                    it.vote_average = (it.vote_average * 10).roundToInt() / 10.0
                }
                recommendedFilms
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
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, 450)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
            Glide.with(this@FilmDetailsFragment)
                .load(BASE_URL_FOR_IMAGES + movieDetails.poster_path)
                .apply(requestOptions)
                .apply(requestOptions)
                .into(filmPoster)
            filmName.text = movieDetails.title

            var rating = movieDetails.vote_average
            rating = (rating * 10).roundToInt() / 10.0

            binding.ratingOfFilm.text = rating.toString()
            when (rating) {
                in 0.0..3.0 -> binding.ratingOfFilm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rating_under_3_color
                    )
                )

                in 3.1..5.0 -> binding.ratingOfFilm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rating_3_to_5_color
                    )
                )

                in 5.1..7.0 -> binding.ratingOfFilm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rating_5_to_7_color
                    )
                )

                in 7.1..10.0 -> binding.ratingOfFilm.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rating_above_7
                    )
                )
            }

            val date = movieDetails.release_date.split("-")
            val year = date[0]

            val genres = movieDetails.genres
            var genresString = ""
            genres.forEachIndexed { index, genre ->
                if (index <= 1) {
                    genresString = genresString + genre.name + ", "
                }
            }


            val mainDescription =
                ("${year}, $genresString \n ${movieDetails.production_countries[0].name} ${movieDetails.runtime} min" +
                        "${if (movieDetails.adult) "18+" else ""}")

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