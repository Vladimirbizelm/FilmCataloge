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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentFilmDetailsFragmentBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.AddToFavoriteRequest
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
// TODO:( add some images for films https://developer.themoviedb.org/reference/movie-images )

class FilmDetailsFragment : Fragment() {

    private val baseURL = "https://api.themoviedb.org/3/"
    private lateinit var binding: FragmentFilmDetailsFragmentBinding
    private var filmID: Int? = null
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filmID = arguments?.getInt("filmID")
        viewModel = ViewModelProvider(this)[FilmDetailsViewModel::class.java]
        dataStoreManager = DataStoreManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainFilmDetailsLayout.visibility = View.VISIBLE

        binding.backFromFilmDetails.setOnClickListener {
            (activity as MainActivity).hideFilmDetailsFragment()
        }
        binding.addToFavoriteButton.setOnClickListener {
            lifecycleScope.launch {
                filmID?.let { id ->
                    addToFavorite(id, RetrofitClient.api)
                }
                val api = RetrofitClient.api
                val sessionId = dataStoreManager.getSessionId()
                val list = getFavoritePageMoviesCollections(api, sessionId)
                if (list != null) {
                    Log.d("FavoritesFragment", "onCreateView: $list")
                }
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



    private fun restoreOrCreateFragmentView(inflater: LayoutInflater, container: ViewGroup?, api: API) {
        binding = FragmentFilmDetailsFragmentBinding.inflate(inflater, container, false)
        val recommendedFilmsAdapter = NestedRVAdapter()
        viewModel.movieDetails.observe(viewLifecycleOwner) { movieDetails ->
            updateUI(movieDetails)
        }

        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            val reviewsAdapter = ReviewsAdapter()
            reviewsAdapter.setReviews(reviews)
            binding.reviewsRV.adapter = reviewsAdapter
        }
        viewModel.recommendedFilms.observe(viewLifecycleOwner) { movies ->
            recommendedFilmsAdapter.setMovies(movies)
            binding.recommendedFilmsRV.adapter = recommendedFilmsAdapter
        }

        if (viewModel.movieDetails.value == null) {
            filmID?.let { id ->
                lifecycleScope.launch {
                    val movieDetails = fetchMovieDetails(id, api)
                    viewModel.movieDetails.postValue(movieDetails)
                    val reviews = getMovieReviews(id, api)
                    viewModel.reviews.postValue(reviews?.results)
                    val recommendedMovies = getMovieRecommendations(id, api)
                    viewModel.recommendedFilms.postValue(recommendedMovies)
                }
            }
        }

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

        recommendedFilmsAdapter.setListener(object : NestedRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, movie: Movie) {
                Toast.makeText(requireContext(), movie.title, Toast.LENGTH_SHORT).show()
                binding.mainFilmDetailsLayout.visibility = View.GONE
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentHolder, newInstance(movie.id))
                    .addToBackStack("movieDetails").commit()
            }
        })

    }
    private suspend fun getFavoritePageMoviesCollections(api: API, sessionId: String?): ArrayList<Movie>? {
        return withContext(Dispatchers.IO) {
            try {
                api.getFavoriteMovies(21858168, API_KEY, sessionId ?: "").results
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error fetching movie reviews", e)
                null
            }
        }
    }

    private suspend fun addToFavorite(movieId: Int, api: API) {
        val sessionId = dataStoreManager.getSessionId()
        if (sessionId != null) {
            try {
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
                    Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FilmDetailsFragment", "Error adding to favorites", e)
                Toast.makeText(requireContext(), "Error adding to favorites", Toast.LENGTH_SHORT).show()
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
                Log.d("film_details_fragment", "its in here")
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
            val mainDescription = ("${((movieDetails.vote_average * 10).roundToInt() / 10)} ${
                movieDetails.vote_count
            } \n ${movieDetails.release_date}, ${
                movieDetails.genres[0].name
            }, ${movieDetails.genres[1].name} \n ${movieDetails.adult}, ${movieDetails.production_countries[0].name}")

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