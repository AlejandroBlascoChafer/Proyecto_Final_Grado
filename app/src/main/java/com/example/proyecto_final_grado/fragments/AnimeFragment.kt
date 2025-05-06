package com.example.proyecto_final_grado.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.Proyecto_Final_Grado.queries.GetUserAnimeListQuery
import com.Proyecto_Final_Grado.queries.GetUserAnimeListQuery.*
import com.Proyecto_Final_Grado.queries.GetUserProfileInfoQuery
import com.Proyecto_Final_Grado.queries.UpdateProgressMutation
import com.Proyecto_Final_Grado.queries.type.MediaListStatus
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.AnimeAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentAnimeBinding
import com.example.proyecto_final_grado.listeners.OnAddEpClickListener
import com.example.proyecto_final_grado.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment : Fragment(), OnAddEpClickListener {

    private var _binding: FragmentAnimeBinding? = null
    private val binding get() = _binding!!

    private lateinit var animeAdapter: AnimeAdapter

    private lateinit var sessionManager: SessionManager

    private lateinit var apolloClient: ApolloClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        setupRecyclerView()
        setupChipListeners()
        binding.chipWatching.isChecked = true
        loadAnimeList("CURRENT")

    }

    private fun setupRecyclerView() {
        animeAdapter = AnimeAdapter(
            animeList = emptyList(),
            listener = this
        )
        binding.animeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = animeAdapter
        }
    }

    private fun setupChipListeners() {
        binding.chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.chipWatching -> "CURRENT"
                R.id.chipCompleted -> "COMPLETED"
                R.id.chipDropped -> "DROPPED"
                R.id.chipPlanning -> "PLANNING"
                else -> "CURRENT"
            }
            loadAnimeList(status)
        }
    }

    private fun loadAnimeList(status: String) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userNameResponse: ApolloResponse<GetUserProfileInfoQuery.Data> = apolloClient.query(GetUserProfileInfoQuery()).execute()
                val userName = userNameResponse.data?.Viewer?.name
                val response: ApolloResponse<Data> = apolloClient.query(GetUserAnimeListQuery(
                    userName.toString(),
                    status = MediaListStatus.valueOf(status)
                )).execute()
                

                val animeList = response.data?.MediaListCollection?.lists?.get(0)?.entries
                Log.d("AnimeList", "Anime List: $animeList")
                withContext(Dispatchers.Main){
                    animeAdapter.submitList(animeList as List<Entry>)
                }
            } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("Error", "${e.message}")
            }
        }



        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAddEp(mediaId: Int, progress: Int) {
        val newProgress = progress +1
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ApolloResponse<UpdateProgressMutation.Data> = apolloClient.mutation(UpdateProgressMutation(mediaId, Optional.Present(newProgress))).execute()
                val updatedEntry = response.data?.SaveMediaListEntry
                if (updatedEntry != null){
                    loadAnimeList("CURRENT")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }

        }
    }
}

