package com.example.filmcataloge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.filmcataloge.databinding.ActivityMainBinding
import com.example.filmcataloge.netConfiguration.popularMovies.Movie
import com.example.filmcataloge.uiConfiguration.fragments.FavoritesFragment
import com.example.filmcataloge.uiConfiguration.fragments.FilmDetailsFragment
import com.example.filmcataloge.uiConfiguration.fragments.MainPageFragment
import com.example.filmcataloge.uiConfiguration.fragments.ProfileFragment
import com.example.filmcataloge.uiConfiguration.fragments.SearchFragment
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel


// TODO: fill profile page with some activity
// TODO: add lists for films and accessibility to add it from movieDetails fragment
// TODO: add more content to the main page
// TODO: add avatar to userAcc layout
// TODO: try to add customView for circle diagram xd
// TODO: option buttons in fragment film details - add logic
// TODO: add fragment for more options button in movie detail fragment

// TODO: add splash screen - fix it
// TODO: profile fragment change login password from tests

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
        //enableEdgeToEdge()
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
    override fun onResume() {
        super.onResume()

        val fragments = listOf(homeFragment, profileFragment, searchFragment, favoritesFragment)
        for (fragment in fragments) {
            if (fragment != activeFragment && fragment.isVisible) {
                supportFragmentManager.beginTransaction()
                    .hide(fragment)
                    .commit()
            }
        }

        if (!activeFragment.isVisible) {
            supportFragmentManager.beginTransaction()
                .show(activeFragment)
                .commit()
        }
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
        val fragments = listOf(homeFragment, profileFragment, searchFragment, favoritesFragment)
        for (fragment in fragments) {
            if (fragment != activeFragment && fragment.isVisible) {
                supportFragmentManager.beginTransaction()
                    .hide(fragment)
                    .commit()
            }
        }
    }

    private fun switchToFragment(fragment: Fragment) {
        if (activeFragment != fragment) {
            clearBackStack()

            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit()
            activeFragment = fragment
        }
    }
    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
    }

    fun showFilmDetailsFragment(movie: Movie) {
        val filmDetailsFragment = FilmDetailsFragment.newInstance(movie)
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .add(R.id.fragmentHolder, filmDetailsFragment, "filmDetails")
            .addToBackStack("filmDetails")
            .commit()
        activeFragment = filmDetailsFragment
    }


    fun hideFilmDetailsFragment(fragmentName: String) {
        if (supportFragmentManager.isStateSaved) return

        supportFragmentManager.popBackStack()

        val targetFragment = when (fragmentName) {
            "home" -> homeFragment
            "profile" -> profileFragment
            "search" -> searchFragment
            "favorites" -> favoritesFragment
            else -> homeFragment
        }

        supportFragmentManager.findFragmentByTag("filmDetails")?.let { detailsFragment ->
            supportFragmentManager.beginTransaction()
                .remove(detailsFragment)
                .commit()
        }

        supportFragmentManager.beginTransaction()
            .show(targetFragment)
            .commit()

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
                switchToFragment(profileFragment)
                binding.bottomNavigation.selectedItemId = R.id.nav_profile
            }
        }
    }

    private fun bottomNavigationMenuLogic() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (activeFragment != homeFragment) {
                        switchToFragment(homeFragment)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (activeFragment != profileFragment) {
                        switchToFragment(profileFragment)
                    }
                    true
                }
                R.id.nav_search -> {
                    if (activeFragment != searchFragment) {
                        switchToFragment(searchFragment)
                    }
                    true
                }
                R.id.nav_favorites -> {
                    if (activeFragment != favoritesFragment) {
                        switchToFragment(favoritesFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }

}