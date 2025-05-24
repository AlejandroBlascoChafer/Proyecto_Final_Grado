package com.example.proyecto_final_grado.fragments.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.example.proyecto_final_grado.adapters.homeAdapters.WeeklyScheduleAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentWeeklyScheduleBinding
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.models.WeeklyScheduleEntry
import com.example.proyecto_final_grado.models.WeeklyScheduleItem
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import graphql.GetAiringScheduleQuery
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId


class WeeklyScheduleFragment : Fragment(), OnAnimeClickListener {

    private var _binding: FragmentWeeklyScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private lateinit var adapter: WeeklyScheduleAdapter

    // Cambiar esto con un botón más adelante
    private var onlyOnList = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = WeeklyScheduleAdapter(this)
        binding.recyclerWeekly.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWeekly.adapter = adapter

        binding.buttonToggleUserList.setOnClickListener {
            onlyOnList = !onlyOnList
            binding.buttonToggleUserList.text = if (onlyOnList) "Show My List Only" else "Show All"
            loadSchedule()
        }

        loadSchedule()
    }

    private fun loadSchedule() {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apolloClient.query(
                    GetAiringScheduleQuery(onList = Optional.presentIfNotNull(onlyOnList.takeIf { it }))
                ).fetchPolicy(FetchPolicy.CacheFirst).execute()



                val items = response.data?.Page?.media?.filterNotNull()?.filter {
                    val status = it.mediaListEntry?.status?.rawValue
                    !onlyOnList || status != "DROPPED"
                } ?: return@launch

                val entries = items.mapNotNull { media ->
                    val airing = media.nextAiringEpisode ?: return@mapNotNull null
                    val date = toLocalZonedDateTime(airing.airingAt) ?: return@mapNotNull null

                    WeeklyScheduleEntry(
                        id = media.id,
                        dayOfWeek = date.dayOfWeek,
                        title = media.title?.userPreferred.orEmpty(),
                        episode = airing.episode,
                        time = date.toLocalTime(),
                        imageUrl = media.coverImage?.large
                    )
                }

                val grouped: Map<DayOfWeek, List<WeeklyScheduleEntry>> = entries
                    .groupBy { it.dayOfWeek }
                    .toSortedMap()

                val result = grouped.flatMap { (day, entryList) ->
                    listOf(WeeklyScheduleItem.Header(day)) +
                            entryList.sortedBy { it.time }
                                .map { WeeklyScheduleItem.Episode(it) }
                }

                adapter.submitList(result)

            } catch (e: Exception) {
                Log.e("WeeklySchedule", "Error loading schedule", e)
            }
        }
    }

    private fun toLocalZonedDateTime(timestampSeconds: Int): org.threeten.bp.ZonedDateTime? {
        val instant = Instant.ofEpochSecond(timestampSeconds.toLong())
        return instant.atZone(ZoneId.systemDefault())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }
}

