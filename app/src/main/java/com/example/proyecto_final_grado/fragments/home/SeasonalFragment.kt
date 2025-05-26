package com.example.proyecto_final_grado.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_final_grado.adapters.homeAdapters.SeasonalAdapter
import com.example.proyecto_final_grado.databinding.FragmentSeasonalBinding
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import graphql.type.MediaFormat
import graphql.type.MediaSeason

class SeasonalFragment : Fragment(), OnAnimeClickListener {

    private var _binding: FragmentSeasonalBinding? = null
    private val binding get() = _binding!!

    private val seasonsList = listOf("WINTER", "SPRING", "SUMMER", "FALL")
    private val yearsList = (1950..(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 2)).toList()

    private val sortByOptions = listOf("Popularity", "Score", "Favorites", "Trending")
    private val sortOrderOptions = listOf("Descending", "Ascending")

    private var filtersVisible = false

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSeasonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialSeason = arguments?.getString("season") ?: viewModel.season.value?.name ?: "WINTER"
        val initialYear = arguments?.getInt("year") ?: viewModel.year.value ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        setupUI(initialSeason, initialYear)

        viewModel.seasonalAnimeList.observe(viewLifecycleOwner, Observer { animeList ->
            if (binding.animeRecyclerView.adapter == null) {
                binding.animeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.animeRecyclerView.adapter = SeasonalAdapter(animeList, this)
            } else {
                (binding.animeRecyclerView.adapter as SeasonalAdapter).updateList(animeList)
            }
            binding.animeRecyclerView.scrollToPosition(0)
        })

        viewModel.season.observe(viewLifecycleOwner) { season ->
            val index = seasonsList.indexOf(season.name)
            if (index >= 0 && binding.spinnerSeason.selectedItemPosition != index) {
                binding.spinnerSeason.setSelection(index)
            }
        }

        viewModel.year.observe(viewLifecycleOwner) { year ->
            val index = yearsList.indexOf(year)
            if (index >= 0 && binding.spinnerYear.selectedItemPosition != index) {
                binding.spinnerYear.setSelection(index)
            }
        }

        viewModel.format.observe(viewLifecycleOwner) { format ->
            val chipId = when (format) {
                MediaFormat.TV -> binding.chipTV.id
                MediaFormat.TV_SHORT -> binding.chipTVSHORT.id
                MediaFormat.MOVIE -> binding.chipMOVIE.id
                MediaFormat.OVA -> binding.chipOVA.id
                MediaFormat.ONA -> binding.chipONA.id
                MediaFormat.SPECIAL -> binding.chipSPECIAL.id
                else -> binding.chipTV.id
            }
            if (!binding.chipGroupFormat.checkedChipIds.contains(chipId)) {
                binding.chipGroupFormat.check(chipId)
            }
        }

        viewModel.sortBy.observe(viewLifecycleOwner) { sortBy ->
            val index = sortByOptions.indexOf(sortBy)
            if (index >= 0 && binding.spinnerSortBy.selectedItemPosition != index) {
                binding.spinnerSortBy.setSelection(index)
            }
        }

        viewModel.sortOrder.observe(viewLifecycleOwner) { sortOrder ->
            val index = sortOrderOptions.indexOf(sortOrder)
            if (index >= 0 && binding.spinnerSortOrder.selectedItemPosition != index) {
                binding.spinnerSortOrder.setSelection(index)
            }
        }

        if (viewModel.seasonalAnimeList.value.isNullOrEmpty()) {
            viewModel.loadSeasonalAnime()
        }

        updateFilterInfo()
    }

    private fun setupUI(initialSeason: String, initialYear: Int) {
        binding.spinnerSeason.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, seasonsList)
        binding.spinnerSeason.setSelection(seasonsList.indexOf(initialSeason.uppercase()))

        binding.spinnerYear.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, yearsList)
        val yearIndex = yearsList.indexOf(initialYear)
        if (yearIndex >= 0) binding.spinnerYear.setSelection(yearIndex)

        binding.chipGroupFormat.setOnCheckedChangeListener(null)
        val currentFormat = viewModel.format.value ?: MediaFormat.TV
        val chipId = when (currentFormat) {
            MediaFormat.TV -> binding.chipTV.id
            MediaFormat.TV_SHORT -> binding.chipTVSHORT.id
            MediaFormat.MOVIE -> binding.chipMOVIE.id
            MediaFormat.OVA -> binding.chipOVA.id
            MediaFormat.ONA -> binding.chipONA.id
            MediaFormat.SPECIAL -> binding.chipSPECIAL.id
            else -> binding.chipTV.id
        }
        binding.chipGroupFormat.check(chipId)

        binding.chipGroupFormat.setOnCheckedChangeListener { _, checkedId ->
            val newFormat = when (checkedId) {
                binding.chipTV.id -> MediaFormat.TV
                binding.chipTVSHORT.id -> MediaFormat.TV_SHORT
                binding.chipMOVIE.id -> MediaFormat.MOVIE
                binding.chipOVA.id -> MediaFormat.OVA
                binding.chipONA.id -> MediaFormat.ONA
                binding.chipSPECIAL.id -> MediaFormat.SPECIAL
                else -> MediaFormat.TV
            }
            if (newFormat != viewModel.format.value) {
                viewModel.setSeasonalFilters(format = newFormat)
                updateFilterInfo()
            }
        }

        val sortByOptions = listOf("Popularity", "Score", "Favorites", "Trending")
        val sortOrderOptions = listOf("Descending", "Ascending")

        binding.spinnerSortBy.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortByOptions)
        binding.spinnerSortOrder.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOrderOptions)

        binding.spinnerSortBy.setSelection(sortByOptions.indexOf(viewModel.sortBy.value ?: "Popularity"))
        binding.spinnerSortOrder.setSelection(sortOrderOptions.indexOf(viewModel.sortOrder.value ?: "Descending"))

        binding.spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long,
            ) {
                val newSortBy = sortByOptions[position]
                if (newSortBy != viewModel.sortBy.value) {
                    viewModel.setSeasonalFilters(sortBy = newSortBy)
                    updateFilterInfo()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerSortOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long,
            ) {
                val newSortOrder = sortOrderOptions[position]
                if (newSortOrder != viewModel.sortOrder.value) {
                    viewModel.setSeasonalFilters(sortOrder = newSortOrder)
                    updateFilterInfo()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long,
            ) {
                val newSeason = seasonsList[position]
                if (newSeason != viewModel.season.value?.name) {
                    viewModel.setSeasonalFilters(season = MediaSeason.valueOf(newSeason))
                    updateFilterInfo()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long,
            ) {
                val newYear = yearsList[position]
                if (newYear != viewModel.year.value) {
                    viewModel.setSeasonalFilters(year = newYear)
                    updateFilterInfo()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnToggleFilters.setOnClickListener {
            filtersVisible = !filtersVisible
            binding.layoutFilters.visibility = if (filtersVisible) View.VISIBLE else View.GONE
            binding.textFilterInfo.visibility = if (filtersVisible) View.GONE else View.VISIBLE
        }
    }

    private fun updateFilterInfo() {
        val seasonName = viewModel.season.value?.name ?: "Unknown"
        val year = viewModel.year.value ?: 0
        binding.textFilterInfo.text = "$seasonName $year"
    }

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
