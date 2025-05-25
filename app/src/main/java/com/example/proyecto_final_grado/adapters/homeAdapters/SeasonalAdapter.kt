package com.example.proyecto_final_grado.adapters.homeAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemSeasonalBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.squareup.picasso.Picasso
import graphql.GetSeasonalAnimeQuery

class SeasonalAdapter(
    private var animeList: List<GetSeasonalAnimeQuery.Medium?>,
    private val listenerAnime: OnAnimeClickListener
) : RecyclerView.Adapter<SeasonalAdapter.SeasonalAnimeViewHolder>() {

    inner class SeasonalAnimeViewHolder(val binding: ItemSeasonalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonalAnimeViewHolder {
        val binding = ItemSeasonalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeasonalAnimeViewHolder(binding)
    }

    override fun getItemCount(): Int = animeList.size

    override fun onBindViewHolder(holder: SeasonalAnimeViewHolder, position: Int) {
        val anime = animeList[position]

        val binding = holder.binding

        Picasso.get().load(anime?.coverImage?.large).into(binding.ivCover)

        binding.tvTitle.text = anime?.title?.userPreferred

        binding.tvStudio.text = anime?.studios?.edges
            ?.firstOrNull { it?.isMain == true }
            ?.node?.name ?: "Unknown"

        binding.tvScore.text = anime?.meanScore?.toString() ?: "-"
        binding.tvFavs.text = anime?.favourites?.toString() ?: "-"

        binding.main.setOnClickListener {
            anime?.id?.let { id ->
                listenerAnime.onAnimeClick(id)
            }
        }
    }

    fun updateList(newList: List<GetSeasonalAnimeQuery.Medium?>) {
        animeList = newList
        notifyDataSetChanged()
    }
}
