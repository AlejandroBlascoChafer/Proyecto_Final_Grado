package com.example.proyecto_final_grado.adapters.homeAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemSeasonalBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.squareup.picasso.Picasso
import graphql.GetSeasonalAnimeQuery

class SeasonalAdapter(
    private var animeList: List<GetSeasonalAnimeQuery.Medium?>,
    private val listenerAnime: OnAnimeClickListener
) : RecyclerView.Adapter<SeasonalAdapter.SeasonalAnimeViewHolder>() {

    private lateinit var binding: ItemSeasonalBinding

    inner class SeasonalAnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cover: ImageView = binding.ivCover
        val title: TextView = binding.tvTitle
        val studio: TextView = binding.tvStudio
        val score: TextView = binding.tvScore
        val favs: TextView = binding.tvFavs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonalAnimeViewHolder {
        binding = ItemSeasonalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeasonalAnimeViewHolder(binding.root)
    }

    override fun getItemCount(): Int = animeList.size

    override fun onBindViewHolder(holder: SeasonalAnimeViewHolder, position: Int) {
        val anime = animeList[position]

        Picasso.get().load(anime?.coverImage?.large).into(holder.cover)

        holder.title.text = anime?.title?.userPreferred

        holder.studio.text = anime?.studios?.edges
            ?.firstOrNull { it?.isMain == true }
            ?.node?.name ?: "Unknown"

        holder.score.text = anime?.meanScore?.toString() ?: "-"

        holder.favs.text = anime?.favourites?.toString() ?: "-"

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
