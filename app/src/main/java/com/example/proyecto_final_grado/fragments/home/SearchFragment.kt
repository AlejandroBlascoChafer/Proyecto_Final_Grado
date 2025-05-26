package com.example.proyecto_final_grado.fragments.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.homeAdapters.SearchAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentSearchBinding
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.fragments.details.CharacterDetailsFragment
import com.example.proyecto_final_grado.fragments.details.MangaDetailsFragment
import com.example.proyecto_final_grado.fragments.details.StaffDetailsFragment
import com.example.proyecto_final_grado.fragments.details.StudiosDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.listeners.OnStudioClickListener
import com.example.proyecto_final_grado.models.SearchItem
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import graphql.SearchAnimeMangaQuery
import graphql.SearchCharactersQuery
import graphql.SearchStaffQuery
import graphql.SearchStudioQuery
import graphql.type.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(), OnCharacterClickListener, OnStaffClickListener,
    OnAnimeClickListener, OnMangaClickListener, OnStudioClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var category: String
    private lateinit var adapter: SearchAdapter
    private var searchJob: Job? = null
    private lateinit var apolloClient: ApolloClient
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        category = arguments?.getString("category") ?: "anime"

        binding.title.text = "Search ${category.replaceFirstChar { it.uppercase() }}"

        adapter = SearchAdapter(emptyList(), this@SearchFragment, this@SearchFragment, this@SearchFragment, this@SearchFragment, this@SearchFragment)
        binding.recyclerSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearch.adapter = adapter

        binding.searchEditText.requestFocus()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                searchJob?.cancel()

                if (query.isNotEmpty()) {
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        performSearch(query)
                    }
                } else {
                    adapter.submitList(emptyList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        lifecycleScope.launch {
            try {
                val allowAdult = viewModel.displayAdultContent.value == true
                val response = when (category.lowercase()) {
                    "anime" -> {
                        apolloClient.query(
                            SearchAnimeMangaQuery(Optional.present(query), Optional.present(MediaType.ANIME))
                        ).execute().data?.Page?.media?.mapNotNull {
                            it?.takeIf { media -> allowAdult || media.isAdult == false }?.let { media ->
                                SearchItem.AnimeMangaItem(
                                    id = media.id,
                                    title = media.title?.userPreferred ?: "No title",
                                    imageUrl = media.coverImage?.large ?: "",
                                    format = media.format?.rawValue,
                                    favourites = media.favourites,
                                    meanScore = media.meanScore,
                                    type = media.type?.name
                                )
                            }
                        }
                    }
                    "manga" -> {
                        apolloClient.query(
                            SearchAnimeMangaQuery(Optional.present(query), Optional.present(MediaType.MANGA))
                        ).execute().data?.Page?.media?.mapNotNull {
                            it?.takeIf { media -> allowAdult || media.isAdult == false }?.let { media ->
                                SearchItem.AnimeMangaItem(
                                    id = media.id,
                                    title = media.title?.userPreferred ?: "No title",
                                    imageUrl = media.coverImage?.large ?: "",
                                    format = media.format?.rawValue,
                                    favourites = media.favourites,
                                    meanScore = media.meanScore,
                                    type = media.type?.name
                                )
                            }
                        }
                    }
                    "characters" -> {
                        apolloClient.query(
                            SearchCharactersQuery(Optional.present(query))
                        ).execute().data?.Page?.characters?.mapNotNull {
                            it?.let { char ->
                                SearchItem.CharacterItem(
                                    id = char.id,
                                    name = char.name?.userPreferred ?: "No name",
                                    imageUrl = char.image?.large ?: "",
                                    favourites = char.favourites
                                )
                            }
                        }
                    }
                    "staff" -> {
                        apolloClient.query(
                            SearchStaffQuery(Optional.present(query))
                        ).execute().data?.Page?.staff?.mapNotNull {
                            it?.let { staff ->
                                SearchItem.StaffItem(
                                    id = staff.id,
                                    name = staff.name?.userPreferred ?: "No name",
                                    imageUrl = staff.image?.large ?: "",
                                    favourites = staff.favourites
                                )
                            }
                        }
                    }
                    "studios" -> {
                        apolloClient.query(
                            SearchStudioQuery(query)
                        ).execute().data?.Page?.studios?.mapNotNull {
                            it?.let { studio ->
                                Log.d("Studio",studio.name + " " + query)
                                val coverImage = studio.media?.nodes?.firstOrNull()?.coverImage?.large ?: ""
                                SearchItem.StudioItem(
                                    id = studio.id,
                                    name = studio.name,
                                    imageUrl = coverImage,
                                    favourites = studio.favourites
                                )
                            }
                        }

                    }
                    else -> emptyList()
                }

                withContext(Dispatchers.Main) {
                    (binding.recyclerSearch.adapter as? SearchAdapter)?.submitList(response ?: emptyList())
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SearchError", "Error: ${e.message}")
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onCharacterClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { CharacterDetailsFragment() }
    }

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }

    override fun onMangaClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { MangaDetailsFragment() }
    }

    override fun onStaffClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { StaffDetailsFragment() }
    }

    override fun onStudioClick(studioName: String) {
        val studiofragment = StudiosDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("NAME", studioName)
            }
        }
        (activity as? MainActivity)?.openDetailFragment(studiofragment)
    }
}
