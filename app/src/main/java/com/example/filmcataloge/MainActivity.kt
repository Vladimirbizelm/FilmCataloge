package com.example.filmcataloge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.filmcataloge.databinding.ActivityMainBinding
import com.example.filmcataloge.uiConfiguration.fragments.FavoritesFragment
import com.example.filmcataloge.uiConfiguration.fragments.FilmDetailsFragment
import com.example.filmcataloge.uiConfiguration.fragments.MainPageFragment
import com.example.filmcataloge.uiConfiguration.fragments.ProfileFragment
import com.example.filmcataloge.uiConfiguration.fragments.SearchFragment
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel

const val API_KEY = "9d84a8a1e699e305c54c15e454163cda"


// TODO: (set up an adapter for recyclerViews), change style for buttons, (add some fragments)
// TODO: (bottom navigation bar, profile page, registration(check API))
// TODO: (work with bottom panel, add logic to the buttons and create more fragments)
// TODO: (add features to fill with fictive data for films, actors, reviews, etc. while it`s not downloaded yet)
// TODO: (create an account page) and add account features
// TODO: (add shared preferences to save user account details and settings) -> added dataStore instead
// TODO: (make main page fragment)
// TODO: (check api and rewrite some code)
// TODO: (add btns logic for filmDetailsFragment)
// TODO: (search panel)
// TODO: https://developer.themoviedb.org/reference/movie-now-playing-list
// TODO: (НАСТРОИТЬ МАСШТАБИРУЕМОСТЬ -> remake all layouts use nested rv)

// TODO: fill profile page with some activity
// TODO: add lists for films and accessibility to add it from movieDetails fragment
// TODO: add more content to the main page
// TODO: optimize date time in filmDetailsFragment and reviewsAdapter
// TODO: check all of the possible exceptions(highly necessary)
// TODO: add avatar to userAcc layout
// TODO: refactor shitty strings file...
// TODO: try to add customView for circle diagram xd
// TODO: option buttons in fragment film details - add logic 
// TODO: add for fav layout that shows u need to log in
// TODO: bug open film detail from favorites adn back
// TODO: make undo for fav button
// TODO: add fragment for more options button in movie detail fragment

// TODO: import btns icons for detail frag and make logic to change it if it was pressed 

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var activeFragment: Fragment
    private lateinit var homeFragment: MainPageFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var favoritesFragment: FavoritesFragment

    private lateinit var filmDetailsViewModel: FilmDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        filmDetailsViewModel = ViewModelProvider(this)[FilmDetailsViewModel::class.java]

        if (savedInstanceState == null) {
            initFragments()
        } else {
            restoreFragments()
        }
        bottomNavigationMenuLogic()
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun initFragments() {
        homeFragment = MainPageFragment.newInstance()
        profileFragment = ProfileFragment.newInstance()
        searchFragment = SearchFragment.newInstance()
        favoritesFragment = FavoritesFragment.newInstance()
        activeFragment = homeFragment

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentHolder, homeFragment, "home")
            .add(R.id.fragmentHolder, profileFragment, "profile").hide(profileFragment)
            .add(R.id.fragmentHolder, searchFragment, "search").hide(searchFragment)
            .add(R.id.fragmentHolder, favoritesFragment, "favorites").hide(favoritesFragment)
            .commit()
    }

    private fun restoreFragments() {

        supportFragmentManager.findFragmentByTag("home")?.let {
            homeFragment = it as MainPageFragment
        } ?: run {
            homeFragment = MainPageFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragmentHolder, homeFragment, "home")
                .commit()
        }

        supportFragmentManager.findFragmentByTag("profile")?.let {
            profileFragment = it as ProfileFragment
        } ?: run {
            profileFragment = ProfileFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentHolder, profileFragment, "profile").hide(profileFragment).commit()
        }

        supportFragmentManager.findFragmentByTag("search")?.let {
            searchFragment = it as SearchFragment
        } ?: run {
            searchFragment = SearchFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentHolder, searchFragment, "search").hide(searchFragment).commit()
        }

        supportFragmentManager.findFragmentByTag("favorites")?.let {
            favoritesFragment = it as FavoritesFragment
        } ?: run {
            favoritesFragment = FavoritesFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentHolder, favoritesFragment, "favorites").hide(favoritesFragment)
                .commit()
        }

        activeFragment = when {
            homeFragment.isVisible -> homeFragment
            profileFragment.isVisible -> profileFragment
            searchFragment.isVisible -> searchFragment
            favoritesFragment.isVisible -> favoritesFragment
            else -> homeFragment
        }
    }

    private fun switchToFragment(fragment: Fragment) {
        if (activeFragment != fragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit()
            activeFragment = fragment
        }
    }

    fun showFilmDetailsFragment(filmID: Int) {
        val filmDetailsFragment = FilmDetailsFragment.newInstance(filmID)
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentHolder, filmDetailsFragment, "filmDetails")
            hide(activeFragment)
        }.addToBackStack("filmDetails").commit()
        activeFragment = filmDetailsFragment
    }

    fun hideFilmDetailsFragment(fragmentName: String) {
        supportFragmentManager.popBackStack()
        val targetFragment = when (fragmentName) {
            "home" -> homeFragment
            "profile" -> profileFragment
            "search" -> searchFragment
            "favorites" -> favoritesFragment
            else -> homeFragment
        }

        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            show(targetFragment)
        }.commit()

        activeFragment = targetFragment

        val menuItemId = when (fragmentName) {
            "home" -> R.id.nav_home
            "profile" -> R.id.nav_profile
            "search" -> R.id.nav_search
            "favorites" -> R.id.nav_favorites
            else -> R.id.nav_home
        }
        binding.bottomNavigation.selectedItemId = menuItemId

    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.toString().startsWith("myapp://auth")) {
                supportFragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(profileFragment)
                    .addToBackStack("profile")
                    .commit()
                activeFragment = profileFragment
                binding.bottomNavigation.selectedItemId = R.id.nav_profile
            }
        }
    }

    private fun bottomNavigationMenuLogic() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchToFragment(homeFragment)
                    true
                }

                R.id.nav_profile -> {
                    switchToFragment(profileFragment)
                    true
                }

                R.id.nav_search -> {
                    switchToFragment(searchFragment)
                    true
                }

                R.id.nav_favorites -> {
                    switchToFragment(favoritesFragment)
                    filmDetailsViewModel.notifyFavoriteMoviesUpdated()
                    true
                }

                else -> false
            }
        }
    }

}