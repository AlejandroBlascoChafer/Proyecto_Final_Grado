package com.example.proyecto_final_grado.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Proyecto_Final_Grado.queries.GetUserAnimeListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaBinding
import com.squareup.picasso.Picasso

class AnimeAdapter(
    private var animeList: List<GetUserAnimeListQuery.Entry>
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
            binding.tvTitle.text = anime.media?.title?.romaji
            binding.tvScore.text = anime.score?.toString() ?: "Sin puntuación"
            binding.tvStatus.text = anime.status?.name
            binding.tvProgress.text = "Progreso: ${anime.progress ?: 0}"

            Picasso.get()
                .load(anime.media?.coverImage?.medium)
                .fit()
                .centerCrop()
                .into(binding.ivCover)
        }
    }
}
