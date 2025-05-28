package com.example.proyecto_final_grado.adapters.mainlist

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemMediaFullAnimeBinding
import graphql.GetUserAnimeListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaSimpleBinding
import com.example.proyecto_final_grado.listeners.OnAddEpClickListener
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnEditListClickListener
import com.example.proyecto_final_grado.listeners.OnScoreClickListener
import com.example.proyecto_final_grado.models.EditListEntryItem
import com.squareup.picasso.Picasso
import graphql.type.MediaListStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale


class AnimeAdapter(
    private var animeList: List<GetUserAnimeListQuery.Entry>,
    private val listener: OnAddEpClickListener,
    private val listenerScore: OnScoreClickListener,
    private val listenerAnime: OnAnimeClickListener,
    private val listenerEditList: OnEditListClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_FULL = 0
        private const val VIEW_TYPE_SIMPLE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (animeList[position].status) {
            MediaListStatus.CURRENT -> VIEW_TYPE_FULL
            else -> VIEW_TYPE_SIMPLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FULL -> {
                val binding = ItemMediaFullAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                FullViewHolder(binding)
            }
            else -> {
                val binding = ItemMediaSimpleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SimpleViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = animeList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val anime = animeList[position]
        when (holder) {
            is FullViewHolder -> holder.bind(anime)
            is SimpleViewHolder -> holder.bind(anime)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<GetUserAnimeListQuery.Entry>) {
        animeList = newList
        notifyDataSetChanged()
    }

    inner class FullViewHolder(private val binding: ItemMediaFullAnimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.root.context
        fun bind(anime: GetUserAnimeListQuery.Entry) {
            val totalEpisodes = anime.media?.episodes
            val episodesWatched = anime.progress ?: 0

            binding.tvProgress.text = if (totalEpisodes != null) {
                "$episodesWatched / $totalEpisodes"
            } else {
                "$episodesWatched / ? "
            }


            val percentage = if (totalEpisodes != null && totalEpisodes > 0) {
                episodesWatched * 100 / totalEpisodes
            } else if (totalEpisodes == null) {
                50
            } else {
                0
            }

            binding.progressBar.progress = percentage
            val formatAiring: String = if (anime.media?.nextAiringEpisode != null ){
                anime.media.format?.name + " · Ep " + anime.media.nextAiringEpisode.episode + " " + formatAiringDateCompat(anime.media.nextAiringEpisode.airingAt)
            } else {
                anime.media?.format?.name.toString()
            }
            binding.tvTitle.text = anime.media?.title?.userPreferred
            binding.tvScore.text = formatScore(anime.score)
            binding.tvMedia.text = formatAiring

            Picasso.get()
                .load(anime.media?.coverImage?.large)
                .fit()
                .centerCrop()
                .into(binding.ivCover)

            binding.btnAddEpisode.setOnClickListener {
                val mediaId = anime.mediaId
                val progress = anime.progress
                if (progress != null) {
                    listener.onAddEp(mediaId, progress)
                }
            }
            binding.tvScore.setOnClickListener{
                val score = anime.score
                val mediaId = anime.mediaId
                val status = anime.status.toString()
                if (score != null){
                    listenerScore.onScoreClick(score, mediaId, status, TextView(context))
                }
            }
            binding.ivCover.setOnClickListener {
                val mediaId = anime.mediaId
                listenerAnime.onAnimeClick(mediaId)
            }
            binding.layoutEdit.setOnClickListener {
                val editList = EditListEntryItem(
                    mediaListEntryId = anime.id,
                    mediaID = anime.mediaId,
                    title = anime.media?.title?.userPreferred,
                    status = anime.status.toString(),
                    score = anime.score,
                    episode = anime.progress,
                    startDate = LocalDate.of(
                            anime.startedAt?.year ?: 1,
                            anime.startedAt?.month ?: 1,
                            anime.startedAt?.day ?: 1
                        ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ,
                    endDate = LocalDate.of(
                        anime.completedAt?.year ?: 1,
                        anime.completedAt?.month ?: 1,
                        anime.completedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    rewatches = anime.repeat,
                    review = anime.notes,
                    private = anime.private,
                    hideFromList = anime.hiddenFromStatusLists,
                    favourite = anime.media?.isFavourite,
                    type = "ANIME"
                )
                Log.d("end date", anime.completedAt?.year.toString() + anime.completedAt?.month.toString() + anime.completedAt?.day.toString())
                listenerEditList.onEditListListener(editList)
            }
        }
    }

    inner class SimpleViewHolder(private val binding: ItemMediaSimpleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.root.context
        fun bind(anime: GetUserAnimeListQuery.Entry) {
            val totalEpisodes = anime.media?.episodes
            val episodesWatched = anime.progress ?: 0

            val percentage = if (totalEpisodes != null && totalEpisodes > 0) {
                episodesWatched * 100 / totalEpisodes
            } else if (totalEpisodes == null) {
                50
            } else {
                0
            }

            binding.tvProgress.text = if (totalEpisodes != null) {
                "$episodesWatched / $totalEpisodes"
            } else {
                "$episodesWatched / ?"
            }

            binding.progressBar.progress = percentage
            binding.tvTitle.text = anime.media?.title?.userPreferred
            binding.tvScore.text = formatScore(anime.score)
            binding.tvMedia.text = anime.media?.format?.name

            Picasso.get()
                .load(anime.media?.coverImage?.large)
                .fit()
                .centerCrop()
                .into(binding.ivCover)


            binding.tvScore.setOnClickListener{
                val score = anime.score
                val mediaId = anime.mediaId
                val status = anime.status.toString()
                if (score != null){
                    listenerScore.onScoreClick(score, mediaId, status, TextView(context))
                }
            }
            binding.ivCover.setOnClickListener {
                val mediaId = anime.mediaId
                listenerAnime.onAnimeClick(mediaId)
            }
            binding.layoutEdit.setOnClickListener {
                val editList = EditListEntryItem(
                    mediaListEntryId = anime.id,
                    mediaID = anime.mediaId,
                    title = anime.media?.title?.userPreferred,
                    status = anime.status.toString(),
                    score = anime.score,
                    episode = anime.progress,
                    startDate = LocalDate.of(
                        anime.startedAt?.year ?: 1,
                        anime.startedAt?.month ?: 1,
                        anime.startedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ,
                    endDate = LocalDate.of(
                        anime.completedAt?.year ?: 1,
                        anime.completedAt?.month ?: 1,
                        anime.completedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    rewatches = anime.repeat,
                    review = anime.notes,
                    private = anime.private,
                    hideFromList = anime.hiddenFromStatusLists,
                    favourite = anime.media?.isFavourite,
                    type = "ANIME"
                )
                Log.d("end date", anime.completedAt?.year.toString() + anime.completedAt?.month.toString() + anime.completedAt?.day.toString())
                listenerEditList.onEditListListener(editList)
            }
        }
    }


    fun formatAiringDateCompat(timestampSeconds: Int): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM, yyyy, hh:mm a", Locale.getDefault())
        val zonedDateTime = Instant.ofEpochSecond(timestampSeconds.toLong())
            .atZone(ZoneId.systemDefault())
        return formatter.format(zonedDateTime)
    }

    fun formatScore(score: Double?): String {
        if (score != null) {
            return if (score % 1.0 == 0.0) {
                score.toInt().toString()
            } else {
                score.toString()
            }
        }
        return ""
    }



}
