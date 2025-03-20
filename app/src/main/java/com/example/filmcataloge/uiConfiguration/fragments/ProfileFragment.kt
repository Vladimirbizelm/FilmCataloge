package com.example.filmcataloge.uiConfiguration.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmcataloge.MainActivity
import com.example.filmcataloge.databinding.FragmentProfileBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.createSession.LoginRequest
import com.example.filmcataloge.netConfiguration.createSession.SessionRequest
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.moviesAdapter.SearchFilmsAdapter
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import com.example.filmcataloge.utils.MovieUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var collectionsRepository: CollectionsRepository
    private lateinit var adapter: SearchFilmsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(requireContext())
        viewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        collectionsRepository =
            CollectionsRepository(RetrofitClient.api, dataStoreManager, requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        setupObservers()

        lifecycleScope.launch {
            if (dataStoreManager.getSessionId() != null) {
                loadWatchedMovies()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (dataStoreManager.getSessionId() != null) {
                loadWatchedMovies()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            val token = dataStoreManager.getRequestToken()
            val sessionId = dataStoreManager.getSessionId()
            Log.d("profile", "token: $token, sessionId: $sessionId")
            when {
                token != null && sessionId == null -> {
                    binding.loginInTMDB.visibility = View.GONE
                    binding.loginIntoAccountLayout.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "token sessionId null", Toast.LENGTH_SHORT)
                        .show()
                }

                sessionId != null && token != null -> {
                    binding.loginLayout.visibility = View.GONE
                    binding.loginIntoAccountLayout.visibility = View.GONE
                    binding.verifiedLayout.visibility = View.VISIBLE

                    Toast.makeText(requireContext(), "token sessionId", Toast.LENGTH_SHORT).show()
                }

                else -> Toast.makeText(
                    requireContext(),
                    "error ${token} $sessionId",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }


    private fun setupRecyclerView() {
        binding.watchedMoviesListRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = SearchFilmsAdapter()
        adapter.setListener(
            object : SearchFilmsAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, movie: Movie) {
                    viewModel.previousFragment.value = "profile"
                    (activity as MainActivity).showFilmDetailsFragment(movie)
                }
            }
        )
        binding.watchedMoviesListRV.adapter = adapter
    }

    private fun setupButtons() {
        val api = RetrofitClient.api

        binding.loginInTMDB.setOnClickListener {
            lifecycleScope.launch {
                val token = getRequestToken(api)
                if (token != null) {
                    dataStoreManager.saveRequestToken(token)
                    openAuthUrl(token)
                } else {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.logIntoAccountBtn.setOnClickListener {
            val username = binding.userNameLogin.text.toString()
            val password = binding.password.text.toString()
            lifecycleScope.launch {
                val sessionId = login(username, password, api)
                if (sessionId != null) {
                    binding.apply {
                        loginIntoAccountLayout.visibility = View.GONE
                        loginLayout.visibility = View.GONE
                        verifiedLayout.visibility = View.VISIBLE
                        userName.text = username
                    }
                    loadWatchedMovies()
                } else {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.historyUpdated.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                if (dataStoreManager.getSessionId() != null) {
                    loadWatchedMovies()
                }
            }
        }
    }

    private suspend fun loadWatchedMovies() {
        try {
            val movies = getWatchedList()?.take(5)
            movies?.let {
                MovieUtils.formatMoviesList(it)
                MovieUtils.formatMovieDate(it)
                adapter.setMovies(it)
            }
            getCollections()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading watched movies", e)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }

    private suspend fun login(username: String, password: String, api: API): String? {
        try {
            val requestToken = dataStoreManager.getRequestToken()

            val loginResponse = api.validateLogin(
                loginRequest = LoginRequest(username, password, requestToken!!),
            )
            Log.d("ProfileFragment", "Login response: $loginResponse")
            if (!loginResponse.success) return null

            val sessionResponse = api.createSession(
                sessionRequest = SessionRequest(loginResponse.request_token)
            )

            if (sessionResponse.success) {
                Log.d("ProfileFragment", "Saving session id")
                dataStoreManager.saveSessionId(sessionResponse.session_id)
                viewModel.notifySessionIdUpdated()
                return sessionResponse.session_id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private suspend fun getRequestToken(api: API): String? {
        val tokenResponse = api.getRequestToken()
        return if (tokenResponse.success) tokenResponse.request_token else null
    }

    private fun openAuthUrl(token: String) {
        val authUrl = "https://www.themoviedb.org/authenticate/$token?redirect_to=myapp://auth"
        val intent = Intent(Intent.ACTION_VIEW, authUrl.toUri())
        startActivity(intent)
    }

    private suspend fun getWatchedList(): List<Movie> {
        val list = collectionsRepository.findCustomListByName("Watched")
        val listID = list?.id
        val viewedList = listID?.let { collectionsRepository.getCustomListDetails(it) }
        if (viewedList != null){
            return MovieUtils.formatMoviesList(viewedList)
        }
        return emptyList()
    }

    private suspend fun getCollections() = coroutineScope {
        val favoriteDeferred = async { collectionsRepository.getFavoriteMovies() }
        val watchlistDeferred = async { collectionsRepository.getWatchlistMovies() }
        val watchedDeferred = async { getWatchedList() }

        val favoriteMovies = favoriteDeferred.await()
        val watchlistMovies = watchlistDeferred.await()
        val watchedMovies = watchedDeferred.await()

        val favoriteSize = favoriteMovies?.size ?: 0
        val watchlistSize = watchlistMovies?.size ?: 0
        val watchedSize = watchedMovies?.size ?: 0

        Log.d("ProfileFragment", "Favorite Movies: $favoriteSize")
        Log.d("ProfileFragment", "Watchlist Movies: $watchlistSize")
        Log.d("ProfileFragment", "Watched Movies: $watchedSize")

        withContext(Dispatchers.Main) {
            binding.statisticsFavoritesCounter.text = favoriteSize.toString()
            binding.statisticsWatchLaterCounter.text = watchlistSize.toString()
            binding.statisticsWatchedCounter.text = watchedSize.toString()
        }
    }
}