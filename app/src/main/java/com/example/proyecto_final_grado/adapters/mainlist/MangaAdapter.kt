package com.example.proyecto_final_grado.adapters.mainlist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import graphql.GetUserMangaListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaFullMangaBinding
import com.example.proyecto_final_grado.databinding.ItemMediaSimpleBinding
import com.example.proyecto_final_grado.listeners.OnAddChClickListener
import com.example.proyecto_final_grado.listeners.OnEditListClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnScoreClickListener
import com.example.proyecto_final_grado.models.EditListEntryItem
import com.squareup.picasso.Picasso
import graphql.type.MediaListStatus
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

class MangaAdapter(
    private var mangaList: List<GetUserMangaListQuery.Entry>,
    private val listener: OnAddChClickListener,
    private val listenerScore: OnScoreClickListener,
    private val listenerManga: OnMangaClickListener,
    private val listenerEditList: OnEditListClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_FULL = 0
        private const val VIEW_TYPE_SIMPLE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (mangaList[position].status) {
            MediaListStatus.CURRENT -> VIEW_TYPE_FULL
            else -> VIEW_TYPE_SIMPLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FULL -> {
                val binding = ItemMediaFullMangaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    override fun getItemCount(): Int = mangaList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val manga = mangaList[position]
        when (holder) {
            is FullViewHolder -> holder.bind(manga)
            is SimpleViewHolder -> holder.bind(manga)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<GetUserMangaListQuery.Entry>) {
        mangaList = newList
        this.notifyDataSetChanged()
    }

    inner class FullViewHolder(private val binding: ItemMediaFullMangaBinding) :
        RecyclerView.ViewHolder(binding.root) {
            val context: Context = binding.root.context
        fun bind(manga: GetUserMangaListQuery.Entry) {
            val chaptersRead = manga.progress ?: 0
            val totalChapters = manga.media?.chapters
            val volumesRead = manga.progressVolumes ?: 0
            val totalVolumes = manga.media?.volumes ?: "?"
            val volumeText = "$volumesRead / $totalVolumes"

            binding.tvProgress.text = if (totalChapters != null) {
                "$chaptersRead / $totalChapters"
            } else {
                "$chaptersRead / ?"
            }

            val percentage = when {
                totalChapters != null && totalChapters > 0 -> {
                    chaptersRead * 100 / totalChapters
                }
                totalChapters == null -> {
                    50 // Sin datos del total, asumimos progreso medio
                }
                else -> 0
            }
            binding.progressBar.progress = percentage

            binding.tvTitle.text = manga.media?.title?.userPreferred ?: "No title"
            binding.tvScore.text = formatScore(manga.score)
            binding.tvVolumes.text = volumeText
            binding.tvMedia.text = manga.media?.format?.name

            Picasso.get()
                .load(manga.media?.coverImage?.large)
                .fit()
                .centerCrop()
                .into(binding.ivCover)

            binding.volumeLayout.visibility = android.view.View.VISIBLE
            binding.btnAddEpisode.text = "+1 CH"

            binding.btnAddEpisode.setOnClickListener {
                val mediaId = manga.mediaId
                val progress = manga.progress
                if (progress != null) {
                    listener.onAddCh(mediaId, progress)
                }
            }
            binding.btnAddVolume.setOnClickListener {
                val mediaId = manga.mediaId
                val progress = manga.progressVolumes
                if (progress != null) {
                    listener.onAddVo(mediaId, progress)
                }
            }
            binding.tvScore.setOnClickListener{
                val score = manga.score
                val mediaId = manga.mediaId
                val status = manga.status.toString()
                if (score != null){
                    listenerScore.onScoreClick(score, mediaId, status, TextView(context))
                }
            }
            binding.ivCover.setOnClickListener {
                val mediaId = manga.mediaId
                listenerManga.onMangaClick(mediaId)
            }
            binding.layoutEdit.setOnClickListener {
                val editList = EditListEntryItem(
                    mediaListEntryId = manga.id,
                    mediaID = manga.mediaId,
                    title = manga.media?.title?.userPreferred,
                    status = manga.status.toString(),
                    score = manga.score,
                    episode = manga.progress,
                    startDate = LocalDate.of(
                        manga.startedAt?.year ?: 1,
                        manga.startedAt?.month ?: 1,
                        manga.startedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ,
                    endDate = LocalDate.of(
                        manga.completedAt?.year ?: 1,
                        manga.completedAt?.month ?: 1,
                        manga.completedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    rewatches = manga.repeat,
                    review = manga.notes,
                    private = manga.private,
                    hideFromList = manga.hiddenFromStatusLists,
                    favourite = manga.media?.isFavourite,
                    type = "MANGA"
                )
                listenerEditList.onEditListListener(editList)
            }
        }
    }

    inner class SimpleViewHolder(private val binding: ItemMediaSimpleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.root.context
        fun bind(manga: GetUserMangaListQuery.Entry) {
            val volumesRead = manga.progressVolumes ?: 0
            val totalVolumes = manga.media?.volumes ?: "?"
            val volumeText = "$volumesRead / $totalVolumes"

            val totalChapters = manga.media?.chapters
            val chaptersRead = manga.progress ?: 0

            val percentage = when {
                totalChapters != null && totalChapters > 0 -> {
                    chaptersRead * 100 / totalChapters
                }
                totalChapters == null -> {
                    50
                }
                else -> 0
            }

            binding.tvProgress.text = if (totalChapters != null) {
                "$chaptersRead / $totalChapters"
            } else {
                "$chaptersRead / ?"
            }

            binding.tvTitle.text = manga.media?.title?.userPreferred
            binding.tvScore.text = formatScore(manga.score)
            binding.tvMedia.text = manga.media?.format?.name
            binding.tvVolumes.text = volumeText
            binding.progressBar.progress = percentage

            Picasso.get()
                .load(manga.media?.coverImage?.large)
                .fit()
                .centerCrop()
                .into(binding.ivCover)


            binding.tvScore.setOnClickListener{
                val score = manga.score
                val mediaId = manga.mediaId
                val status = manga.status.toString()
                if (score != null){
                    listenerScore.onScoreClick(score, mediaId, status, TextView(context))
                }
            }
            binding.ivCover.setOnClickListener {
                val mediaId = manga.mediaId
                listenerManga.onMangaClick(mediaId)
            }
            binding.layoutEdit.setOnClickListener {
                val editList = EditListEntryItem(
                    mediaListEntryId = manga.id,
                    mediaID = manga.mediaId,
                    title = manga.media?.title?.userPreferred,
                    status = manga.status.toString(),
                    score = manga.score,
                    episode = manga.progress,
                    startDate = LocalDate.of(
                        manga.startedAt?.year ?: 1,
                        manga.startedAt?.month ?: 1,
                        manga.startedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ,
                    endDate = LocalDate.of(
                        manga.completedAt?.year ?: 1,
                        manga.completedAt?.month ?: 1,
                        manga.completedAt?.day ?: 1
                    ).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    rewatches = manga.repeat,
                    review = manga.notes,
                    private = manga.private,
                    hideFromList = manga.hiddenFromStatusLists,
                    favourite = manga.media?.isFavourite,
                    type = "MANGA"
                )
                listenerEditList.onEditListListener(editList)
            }
        }
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
