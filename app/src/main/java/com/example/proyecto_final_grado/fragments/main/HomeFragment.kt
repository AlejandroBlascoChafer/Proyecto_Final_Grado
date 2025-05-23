package com.example.proyecto_final_grado.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.homeAdapters.AnimeTrendAdapter
import com.example.proyecto_final_grado.adapters.homeAdapters.MangaTrendAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentHomeBinding
import com.example.proyecto_final_grado.fragments.SeasonalFragment
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.fragments.details.MangaDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.session.SessionManager
import com.example.proyecto_final_grado.ui.SearchBottomSheet
import com.example.proyecto_final_grado.utils.SharedViewModel
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import com.example.proyecto_final_grado.ui.search.SearchFragment
import com.squareup.picasso.Picasso
import java.util.Calendar

class HomeFragment : Fragment(), OnAnimeClickListener, OnMangaClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private lateinit var apolloClient: ApolloClient

    private lateinit var animeTrendAdapter: AnimeTrendAdapter
    private lateinit var mangaTrendAdapter: MangaTrendAdapter



    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        sharedViewModel.userProfile.observe(viewLifecycleOwner) { viewer ->

            Picasso.get().load(viewer?.avatar?.large).into(binding.imageIcon)
            Picasso.get().load(viewer?.bannerImage).into(binding.imageBackground)

            binding.textWelcome.text = "Welcome ${viewer?.name}"

            binding.searchTrigger.setOnClickListener {
                val bottomSheet = SearchBottomSheet()
                bottomSheet.listener = object : SearchBottomSheet.OnCategorySelectedListener {
                    override fun onCategorySelected(category: String) {
                        val fragment = SearchFragment().apply {
                            arguments = Bundle().apply {
                                putString("category", category)
                            }
                        }
                        (activity as? MainActivity)?.openDetailFragment(fragment)
                    }
                }
                bottomSheet.show(childFragmentManager, "searchCategory")
            }

            binding.seasonFilterCard.setOnClickListener {
                val (season, year) = getCurrentSeasonAndYear()
                val fragment = SeasonalFragment().apply {
                    arguments = Bundle().apply {
                        putString("season", season)
                        putInt("year", year)
                    }
                }
                (activity as? MainActivity)?.openDetailFragment(fragment)
            }

        }
        sharedViewModel.trendingAnime.observe(viewLifecycleOwner) { fullList ->
            if (fullList != null) {
                animeTrendAdapter.updateList(fullList)
            }
        }
        sharedViewModel.trendingManga.observe(viewLifecycleOwner) { fullList ->
            if (fullList != null) {
                mangaTrendAdapter.updateList(fullList)
            }
        }
        setupRecyclerView()
    }

    private fun getCurrentSeasonAndYear(): Pair<String, Int> {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val season = when (month) {
            in 0..2 -> "WINTER"    // enero, febrero, marzo
            in 3..5 -> "SPRING"    // abril, mayo, junio
            in 6..8 -> "SUMMER"    // julio, agosto, septiembre
            else -> "FALL"         // octubre, noviembre, diciembre
        }

        return season to year
    }


    private fun setupRecyclerView() {
        animeTrendAdapter = AnimeTrendAdapter(
            animeList = emptyList(),
            listenerAnime = this@HomeFragment,
            requireContext()
        )

        binding.recyclerAnimeTrending.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = animeTrendAdapter
        }

        mangaTrendAdapter = MangaTrendAdapter(
            mangaList = emptyList(),
            listenerManga = this@HomeFragment,
            requireContext()
        )

        binding.recyclerMangaTrending.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mangaTrendAdapter
        }
    }

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }

    override fun onMangaClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { MangaDetailsFragment() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
