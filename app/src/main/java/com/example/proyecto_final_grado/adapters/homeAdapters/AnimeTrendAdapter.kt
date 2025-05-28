package com.example.proyecto_final_grado.adapters.homeAdapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemTrendingAnimeBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.squareup.picasso.Picasso
import graphql.GetTrendingAnimeQuery

class AnimeTrendAdapter(
    private var animeList: List<GetTrendingAnimeQuery.Medium?>,
    private val listenerAnime: OnAnimeClickListener,
    private val context: Context
) :
    RecyclerView.Adapter<AnimeTrendAdapter.AnimeTrendViewHolder>() {

    private lateinit var binding: ItemTrendingAnimeBinding
    private val markwon = MarkdownUtils

    inner class AnimeTrendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val banner: ImageView = binding.imageBanner
        val cover: ImageView = binding.imageCover
        val title: TextView = binding.textTitle
        val studio: TextView = binding.textStudio
        val genres: TextView = binding.textGenres
        val description: TextView = binding.textDescription
        val score : TextView = binding.textScore
        val favs : TextView = binding.textFavourites

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeTrendViewHolder {
        binding = ItemTrendingAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeTrendViewHolder(binding.root)
    }

    override fun getItemCount(): Int = animeList.size

    override fun onBindViewHolder(holder: AnimeTrendViewHolder, position: Int) {
        val anime = animeList[position]

        Picasso.get().load(anime?.bannerImage).into(holder.banner)
        Picasso.get().load(anime?.coverImage?.large).into(holder.cover)

        holder.title.text = anime?.title?.userPreferred
        holder.studio.text = anime?.studios?.edges
            ?.firstOrNull { it?.isMain == true }
            ?.node?.name ?: ""
        holder.genres.text = anime?.genres.toString()
        markwon.setMarkdownText(context, holder.description, anime?.description)
        holder.score.text = anime?.meanScore.toString()
        holder.favs.text = anime?.favourites.toString()

        binding.container.setOnClickListener{
            val mediaID = anime?.id
            if (mediaID != null) {
                listenerAnime.onAnimeClick(mediaID)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<GetTrendingAnimeQuery.Medium?>) {
        animeList = newList
        notifyDataSetChanged()
    }

}
