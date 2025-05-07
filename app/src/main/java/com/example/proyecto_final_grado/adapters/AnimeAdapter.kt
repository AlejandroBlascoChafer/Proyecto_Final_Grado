package com.example.proyecto_final_grado.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Proyecto_Final_Grado.queries.GetUserAnimeListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaBinding
import com.example.proyecto_final_grado.listeners.OnAddEpClickListener
import com.squareup.picasso.Picasso

class AnimeAdapter(
    private var animeList: List<GetUserAnimeListQuery.Entry>,
    private val listener: OnAddEpClickListener
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(animeList[position])
    }

    override fun getItemCount(): Int = animeList.size

    fun submitList(newList: List<GetUserAnimeListQuery.Entry>) {
        animeList = newList
        notifyDataSetChanged()
    }


    inner class AnimeViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(anime: GetUserAnimeListQuery.Entry) {
            val totalEpisodes = anime.media?.episodes
            val episodesWatched = anime.progress ?: 0

            binding.tvProgress.text = if (totalEpisodes != null) {
                "$episodesWatched / $totalEpisodes episodes"
            } else {
                "$episodesWatched / ? episodes"
            }
            val percentage = when {
                totalEpisodes != null && totalEpisodes > 0 -> {
                    episodesWatched * 100 / totalEpisodes
                }
                totalEpisodes == null -> {
                    50
                }
                else -> 0
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
        }
    }
}
