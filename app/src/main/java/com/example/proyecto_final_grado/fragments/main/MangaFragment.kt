package com.example.proyecto_final_grado.fragments.main

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import graphql.GetUserProfileInfoQuery
import graphql.UpdateProgressMutation
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.exception.ApolloException
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.mainlist.MangaAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.DialogScoreBinding
import com.example.proyecto_final_grado.databinding.FragmentMangaBinding
import com.example.proyecto_final_grado.fragments.EditListEntryFragment
import com.example.proyecto_final_grado.fragments.details.MangaDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAddChClickListener
import com.example.proyecto_final_grado.listeners.OnEditListClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnScoreClickListener
import com.example.proyecto_final_grado.models.EditListEntryItem
import com.example.proyecto_final_grado.session.SessionManager
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import graphql.GetUserMangaListQuery
import graphql.UpdateScoreMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaFragment : Fragment(), OnAddChClickListener, OnScoreClickListener, OnMangaClickListener, OnEditListClickListener {

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
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                sharedViewModel.loadUserMangaList(sessionManager.getUsername().toString())
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        setupRecyclerView()
        setupChipListeners()

        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        sharedViewModel.mangaList.observe(viewLifecycleOwner) { fullList ->
            val currentStatus = getSelectedStatus()
            val filteredList = fullList?.filter { it?.status?.name == currentStatus }
            filteredList?.filterNotNull()?.let { mangaAdapter.submitList(it) }
        }

        sharedViewModel.loadingManga.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading == true) {
                binding.loadingLayout.apply {
                    visibility = View.VISIBLE
                    startAnimation(fadeIn)
                }
            } else {
                binding.loadingLayout.apply {
                    startAnimation(fadeOut)
                    postDelayed({ visibility = View.GONE }, 250)
                }
            }
        }

        binding.chipReading.isChecked = true
    }

    private fun setupRecyclerView() {
        mangaAdapter = MangaAdapter(
            emptyList(),
            listener = this,
            listenerScore = this,
            listenerManga = this,
            listenerEditList = this
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
            filteredList?.filterNotNull()?.let {
                mangaAdapter.submitList(it)
                binding.mangaRecyclerView.scrollToPosition(0)
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
                    sharedViewModel.loadInitialData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error Fetching from network", Toast.LENGTH_SHORT).show()
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
                    sharedViewModel.loadInitialData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error Fetching from network", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onScoreClick(score: Double, mediaId: Int, status: String, scoreText: TextView) {
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
                    Toast.makeText(context, "Error Fetching from network", Toast.LENGTH_SHORT).show()
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
                    .setTitle("Change Score")
                    .setView(binding.root)
                    .setPositiveButton("OK") { _, _ ->
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
                                        sharedViewModel.loadInitialData()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error Fetching from network", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun onMangaClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { MangaDetailsFragment() }
    }

    override fun onEditListListener(entry: EditListEntryItem) {
        val fragment = EditListEntryFragment().apply {
            arguments = Bundle().apply {
                putParcelable("entry", entry)
            }
        }
        (activity as? MainActivity)?.openDetailFragment(fragment)
    }
}

