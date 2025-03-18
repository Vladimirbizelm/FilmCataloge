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
import com.example.filmcataloge.API_KEY
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.FragmentMoreOptionsButtonLayoutBinding
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.createCustomList.CreateCustomListRequest
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.getListOfCustomLists.GetListOfCustomListsResponse
import com.example.filmcataloge.netConfiguration.Lists.addToFavorite.customLists.getListOfCustomLists.ListObject
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import com.example.filmcataloge.uiConfiguration.viewModel.FilmDetailsViewModel
import com.example.filmcataloge.utils.CollectionsRepository
import kotlinx.coroutines.launch


class MoreOptionsButtonFragment : Fragment() {


    private lateinit var binding: FragmentMoreOptionsButtonLayoutBinding
    private lateinit var viewModel: FilmDetailsViewModel
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var collectionsRepository: CollectionsRepository
    private var filmID: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[FilmDetailsViewModel::class.java]
        dataStoreManager = DataStoreManager(requireContext())
        collectionsRepository = CollectionsRepository(RetrofitClient.api, dataStoreManager, requireContext())
        filmID = arguments?.getInt("filmID")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.setOnClickListener {
            viewModel.notifyMoreOptionsFragmentClosed()
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.addToListFilmButton.setOnClickListener {
            Toast.makeText(requireContext(), "pu", Toast.LENGTH_SHORT).show()
        }
        binding.addToWatchedButton.setOnClickListener {
            updateUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoreOptionsButtonLayoutBinding.inflate(inflater, container, false)
        updateUI()
        return binding.root
    }

    private fun updateUI(){
        lifecycleScope.launch {
            val success = collectionsRepository.createListIfNotExists(
                "Watched",
                "List of watched films"
            )
            if (success) {
                val list = collectionsRepository.findCustomListByName("Watched")
                list?.id?.let { listId ->
                    val movieId = filmID
                    if (collectionsRepository.isMovieInCustomList(listId, movieId!!)){
                        collectionsRepository.removeMovieFromCustomList(listId!!, movieId!!)
                        Toast.makeText(requireContext(), "Фильм уже добавлен в список Watched", Toast.LENGTH_SHORT).show()
                        binding.addToWatchedButton.setImageResource(R.drawable.add_to_watched)
                        return@launch
                    } else {
                        collectionsRepository.addMovieToCustomList(listId, movieId!!)
                        Toast.makeText(requireContext(), "Фильм добавлен в список Watched", Toast.LENGTH_SHORT).show()
                        binding.addToWatchedButton.setImageResource(R.drawable.added_to_watched)
                    }

                }
            } else {
                Toast.makeText(requireContext(), "Не удалось создать список", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.notifyMoreOptionsFragmentClosed()
    }

    companion object {
        @JvmStatic
        fun newInstance(filmID: Int): MoreOptionsButtonFragment {
            val fragment = MoreOptionsButtonFragment()
            val args = Bundle()
            args.putInt("filmID", filmID)
            fragment.arguments = args
            return fragment
        }
    }
}