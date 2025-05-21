package com.example.proyecto_final_grado.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.adapters.AnimeTrendAdapter
import com.example.proyecto_final_grado.adapters.MangaTrendAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentHomeBinding
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.fragments.details.MangaDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.session.SessionManager
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.example.proyecto_final_grado.utils.SharedViewModel
import com.example.proyecto_final_grado.utils.openMediaDetailFragment

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
