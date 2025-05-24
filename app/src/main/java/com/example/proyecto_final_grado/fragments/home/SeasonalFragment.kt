package com.example.proyecto_final_grado.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.homeAdapters.SeasonalAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentSeasonalBinding
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import graphql.GetSeasonalAnimeQuery
import graphql.type.MediaFormat
import graphql.type.MediaSeason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeasonalFragment : Fragment(), OnAnimeClickListener {

    private var _binding: FragmentSeasonalBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var season: String = ""
    private var year: Int = 0

    private var filtersVisible = false

    private var selectedFormat: MediaFormat = MediaFormat.TV
    private var selectedSortBy: String = "Popularity"
    private var selectedSortOrder: String = "Descending"

    private val seasonsList = listOf("WINTER", "SPRING", "SUMMER", "FALL")
    private val yearsList = (1950..(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 2)).toList()

    private var yearSelected = year.takeIf { it != 0 } ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeasonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        season = arguments?.getString("season").orEmpty()
        year = arguments?.getInt("year") ?: 0

        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        setupUI()
        loadAnimeForSelectedFormat()
    }

    private fun setupUI() {
        binding.chipTV.isChecked = true
        selectedFormat = MediaFormat.TV


        binding.layoutFilters.visibility = View.GONE
        binding.textFilterInfo.visibility = View.VISIBLE
        filtersVisible = false

        binding.btnToggleFilters.setOnClickListener {
            filtersVisible = !filtersVisible

            binding.layoutFilters.visibility = if (filtersVisible) View.VISIBLE else View.GONE
            binding.textFilterInfo.visibility = if (filtersVisible) View.GONE else View.VISIBLE

            binding.btnToggleFilters.text =
                if (filtersVisible) getString(R.string.hide_filters) else getString(R.string.show_filters)

            if (!filtersVisible) {
                updateFilterInfo()
            }
        }

        binding.chipGroupFormat.setOnCheckedChangeListener { _, checkedId ->
            selectedFormat = when (checkedId) {
                binding.chipTV.id -> MediaFormat.TV
                binding.chipTVSHORT.id -> MediaFormat.TV_SHORT
                binding.chipMOVIE.id -> MediaFormat.MOVIE
                binding.chipOVA.id -> MediaFormat.OVA
                binding.chipONA.id -> MediaFormat.ONA
                binding.chipSPECIAL.id -> MediaFormat.SPECIAL
                else -> MediaFormat.TV
            }
            loadAnimeForSelectedFormat()
            updateFilterInfo()
        }

        val sortByOptions = listOf("Popularity", "Score", "Favorites", "Trending")
        val sortOrderOptions = listOf("Descending", "Ascending")

        binding.spinnerSortBy.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortByOptions
        )
        binding.spinnerSortOrder.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOrderOptions
        )

        binding.spinnerSortBy.setSelection(sortByOptions.indexOf(selectedSortBy))
        binding.spinnerSortOrder.setSelection(sortOrderOptions.indexOf(selectedSortOrder))

        binding.spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedSortBy = sortByOptions[position]
                loadAnimeForSelectedFormat()
                updateFilterInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerSortOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedSortOrder = sortOrderOptions[position]
                loadAnimeForSelectedFormat()
                updateFilterInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.animeRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.spinnerSeason.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, seasonsList)
        binding.spinnerSeason.setSelection(seasonsList.indexOf(season))
        binding.spinnerSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newSeason = seasonsList[position]
                if (newSeason != season) {
                    season = newSeason
                    loadAnimeForSelectedFormat()
                    updateFilterInfo()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        binding.spinnerYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, yearsList)
        val yearIndex = yearsList.indexOf(yearSelected)
        if (yearIndex >= 0) {
            binding.spinnerYear.setSelection(yearIndex)
        }
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newYear = yearsList[position]
                if (newYear != yearSelected) {
                    yearSelected = newYear
                    loadAnimeForSelectedFormat()
                    updateFilterInfo()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun loadAnimeForSelectedFormat() {
        val seasonEnum = when (season.uppercase()) {
            "WINTER" -> MediaSeason.WINTER
            "SPRING" -> MediaSeason.SPRING
            "SUMMER" -> MediaSeason.SUMMER
            "FALL" -> MediaSeason.FALL
            else -> throw IllegalArgumentException("Unknown season: $season")
        }


        lifecycleScope.launch {
            try {
                val response = apolloClient.query(GetSeasonalAnimeQuery(seasonEnum, yearSelected, selectedFormat))
                    .fetchPolicy(FetchPolicy.CacheFirst).execute()
                val mediaList = response.data?.Page?.media?.filterNotNull() ?: emptyList()


                val sortedList = mediaList.sortedWith(compareByDescending {
                    when (selectedSortBy) {
                        "Popularity" -> it.popularity ?: 0
                        "Score" -> it.meanScore ?: 0
                        "Favorites" -> it.favourites ?: 0
                        "Trending" -> it.trending ?: 0
                        else -> 0
                    }
                })

                val finalList = if (selectedSortOrder == "Ascending") sortedList.reversed() else sortedList

                withContext(Dispatchers.Main) {
                    binding.animeRecyclerView.adapter = SeasonalAdapter(finalList, this@SeasonalFragment)
                }

            } catch (e: Exception) {
                Log.e("ResponseError", e.message.toString())
            }
        }
    }

    private fun updateFilterInfo() {
        val seasonDisplay = season.lowercase().replaceFirstChar { it.uppercase() }
        val info = "Format: ${selectedFormat.name} | Season: $seasonDisplay $yearSelected | Sort by: $selectedSortBy ($selectedSortOrder)"
        binding.textFilterInfo.text = info
    }

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
