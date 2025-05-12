package com.example.proyecto_final_grado.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import graphql.GetUserMangaListQuery
import graphql.GetUserMangaListQuery.*
import graphql.GetUserProfileInfoQuery
import graphql.UpdateProgressMutation
import graphql.type.MediaListStatus
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.MangaAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.DialogScoreBinding
import com.example.proyecto_final_grado.databinding.FragmentMangaBinding
import com.example.proyecto_final_grado.listeners.OnAddChClickListener
import com.example.proyecto_final_grado.listeners.OnScoreClickListener
import com.example.proyecto_final_grado.utils.SessionManager
import com.example.proyecto_final_grado.utils.SharedViewModel
import graphql.UpdateScoreMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaFragment : Fragment(), OnAddChClickListener, OnScoreClickListener {

    private var _binding: FragmentMangaBinding? = null
    private val binding get() = _binding!!

    private lateinit var mangaAdapter: MangaAdapter

    private lateinit var sessionManager: SessionManager

    private lateinit var apolloClient: ApolloClient

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        setupRecyclerView()
        setupChipListeners()
        sharedViewModel.mangaList.observe(viewLifecycleOwner) { fullList ->
            val currentStatus = getSelectedStatus()
            val filteredList = fullList?.filter { it?.status?.name == currentStatus }
            filteredList?.filterNotNull()?.let { mangaAdapter.submitList(it) }
        }

        if (sharedViewModel.animeList.value == null) {
            sharedViewModel.loadInitialData()
        }

        binding.chipReading.isChecked = true
    }

    private fun setupRecyclerView() {
        mangaAdapter = MangaAdapter(
            emptyList(),
            listener = this,
            listenerScore = this
        )
        binding.mangaRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mangaAdapter
        }
    }

    private fun getSelectedStatus(): String {
        return when (binding.chipGroupStatus.checkedChipId) {
            R.id.chipReading -> "CURRENT"
            R.id.chipCompleted -> "COMPLETED"
            R.id.chipDropped -> "DROPPED"
            R.id.chipPlanning -> "PLANNING"
            else -> "CURRENT"
        }
    }

    private fun setupChipListeners() {
        binding.chipGroupStatus.setOnCheckedChangeListener { _, _ ->
            val currentStatus = getSelectedStatus()
            val fullList = sharedViewModel.mangaList.value
            val filteredList = fullList?.filter { it?.status?.name == currentStatus }
            filteredList?.filterNotNull()?.let { mangaAdapter.submitList(it) }
        }
    }

    private fun loadMangaList(status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userNameResponse: ApolloResponse<GetUserProfileInfoQuery.Data> = apolloClient.query(GetUserProfileInfoQuery()).execute()
                val userName = userNameResponse.data?.Viewer?.name
                val response: ApolloResponse<Data> = apolloClient.query(GetUserMangaListQuery(
                    userName = userName.toString(),
                    status = MediaListStatus.valueOf(status)
                )).execute()

                val mangaList = response.data?.MediaListCollection?.lists?.get(0)?.entries
                withContext(Dispatchers.Main) {
                    mangaAdapter.submitList(mangaList as List<Entry>)
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

    override fun onAddCh(mediaId: Int, progress: Int) {
        val newProgress = progress +1
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ApolloResponse<UpdateProgressMutation.Data> = apolloClient.mutation(
                    UpdateProgressMutation(mediaId, Optional.Present(newProgress))
                ).execute()
                val updatedEntry = response.data?.SaveMediaListEntry
                if (updatedEntry != null){
                    loadMangaList("CURRENT")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }

        }
    }

    override fun onAddVo(mediaId: Int, progressVolumes: Int) {
        val newProgress = progressVolumes +1
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ApolloResponse<UpdateProgressMutation.Data> = apolloClient.mutation(
                    UpdateProgressMutation(
                        mediaId = mediaId,
                        progress = Optional.Absent,
                        progressVolumes = Optional.Present(newProgress))
                ).execute()
                val updatedEntry = response.data?.SaveMediaListEntry
                if (updatedEntry != null){
                    loadMangaList("CURRENT")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }

        }
    }

    override fun onScoreClick(score: Double, mediaId: Int, status: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_score, null)
        val binding = DialogScoreBinding.bind(dialogView)
        var scoreFormat = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ApolloResponse<GetUserProfileInfoQuery.Data> = apolloClient.query(GetUserProfileInfoQuery()).execute()
                scoreFormat = response.data?.Viewer?.mediaListOptions?.scoreFormat.toString()
                Log.d("ScoreDebug", "score: $score, scoreFormat: $scoreFormat")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }
            withContext(Dispatchers.Main){
                val (max, _) = when (scoreFormat) {
                    "POINT_100" -> 100 to 1
                    "POINT_10_DECIMAL" -> 100 to 1
                    "POINT_10" -> 10 to 1
                    "POINT_5" -> 5 to 1
                    "POINT_3" -> 3 to 1
                    else -> 100 to 1
                }


                binding.seekBarScore.max = max
                binding.seekBarScore.progress = when (scoreFormat) {
                    "POINT_10_DECIMAL" -> (score * 10).toInt()
                    else -> score.toInt()
                }

                fun formatScore(progress: Int): String = when (scoreFormat) {
                    "POINT_10_DECIMAL" -> "%.1f".format(progress / 10f)
                    else -> progress.toString()
                }

                binding.tvCurrentScore.text = "Score: ${formatScore(binding.seekBarScore.progress)}"

                binding.seekBarScore.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        binding.tvCurrentScore.text = "Score: ${formatScore(progress)}"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                AlertDialog.Builder(context)
                    .setTitle("Cambiar puntuación")
                    .setView(binding.root)
                    .setPositiveButton("Aceptar") { _, _ ->
                        val value = binding.seekBarScore.progress
                        val finalScore = when (scoreFormat) {
                            "POINT_10_DECIMAL" -> value / 10f
                            else -> value.toFloat()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response: ApolloResponse<UpdateScoreMutation.Data> = apolloClient.mutation(
                                    UpdateScoreMutation(mediaId, finalScore.toDouble())
                                ).execute()

                                val updatedEntry = response.data?.SaveMediaListEntry
                                if (updatedEntry != null) {
                                    withContext(Dispatchers.Main) {
                                        loadMangaList(status)
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Log.d("Error", "Error al actualizar puntuación: ${e.message}")
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }


    }
}

