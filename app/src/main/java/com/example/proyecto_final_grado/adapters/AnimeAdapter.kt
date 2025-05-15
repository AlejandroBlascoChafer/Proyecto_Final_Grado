package com.example.proyecto_final_grado.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemMediaFullAnimeBinding
import graphql.GetUserAnimeListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaSimpleBinding
import com.example.proyecto_final_grado.listeners.OnAddEpClickListener
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnScoreClickListener
import com.squareup.picasso.Picasso
import graphql.type.MediaListStatus

class AnimeAdapter(
    private var animeList: List<GetUserAnimeListQuery.Entry>,
    private val listener: OnAddEpClickListener,
    private val listenerScore: OnScoreClickListener,
    private val listenerAnime: OnAnimeClickListener
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

    fun submitList(newList: List<GetUserAnimeListQuery.Entry>) {
        animeList = newList
        notifyDataSetChanged()
    }

    inner class FullViewHolder(private val binding: ItemMediaFullAnimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(anime: GetUserAnimeListQuery.Entry) {
            val totalEpisodes = anime.media?.episodes
            val episodesWatched = anime.progress ?: 0

            binding.tvProgress.text = if (totalEpisodes != null) {
                "$episodesWatched / $totalEpisodes"
            } else {
                "$episodesWatched / ?"
            }

            val percentage = if (totalEpisodes != null && totalEpisodes > 0) {
                episodesWatched * 100 / totalEpisodes
            } else if (totalEpisodes == null) {
                50
            } else {
                0
            }

            binding.progressBar.progress = percentage
            binding.tvTitle.text = anime.media?.title?.userPreferred
            binding.tvScore.text = anime.score?.toString() ?: "No score"
            binding.tvMedia.text = anime.media?.format?.name

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
                    listenerScore.onScoreClick(score, mediaId, status)
                }
            }
            binding.ivCover.setOnClickListener {
                val mediaId = anime.mediaId
                listenerAnime.onAnimeClick(mediaId)
            }
        }
    }

    inner class SimpleViewHolder(private val binding: ItemMediaSimpleBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
            binding.tvScore.text = anime.score?.toString() ?: "No score"
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
                    listenerScore.onScoreClick(score, mediaId, status)
                }
            }
            binding.ivCover.setOnClickListener {
                val mediaId = anime.mediaId
                listenerAnime.onAnimeClick(mediaId)
            }
        }
    }
}
