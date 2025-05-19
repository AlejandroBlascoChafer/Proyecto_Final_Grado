
package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemRecommendationsBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class RecommendationsAdapter(
    private val recommendationsList: List<GetMediaDetailQuery.Edge4>,
    private val listenerManga: OnMangaClickListener,
    private val listenerAnime: OnAnimeClickListener
) :
    RecyclerView.Adapter<RecommendationsAdapter.RecommendationsViewHolder>() {
    private lateinit var binding: ItemRecommendationsBinding

    inner class RecommendationsViewHolder(val binding: ItemRecommendationsBinding) : RecyclerView.ViewHolder(binding.root) {
        val cardView: CardView = binding.main
        val imageView: ImageView = binding.ivCover
        val titleView: TextView = binding.tvTitle
        val yearView: TextView = binding.tvYearRelease
        val scoreView: TextView = binding.tvScore
        val favsView: TextView = binding.tvFavs
        val formatEpisodesView: TextView = binding.tvFormatEpisodes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationsViewHolder {
        binding = ItemRecommendationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationsViewHolder, position: Int) {
        val recommendation = recommendationsList[position]
        Picasso.get().load(recommendation.node?.mediaRecommendation?.coverImage?.large).into(holder.imageView)
        holder.titleView.text = recommendation.node?.mediaRecommendation?.title?.userPreferred ?: ""
        holder.yearView.text = (recommendation.node?.mediaRecommendation?.startDate?.year ?: "").toString()
        val format = recommendation.node?.mediaRecommendation?.format
        val chapters = recommendation.node?.mediaRecommendation?.chapters
        val episodes = recommendation.node?.mediaRecommendation?.episodes
        if ( recommendation.node?.mediaRecommendation?.type?.name == "MANGA" ){
            holder.formatEpisodesView.text = if (chapters != null) "$format · ${chapters} chapters" else "$format"
        } else{
            holder.formatEpisodesView.text = if (episodes != null) "$format · ${episodes} episodes" else "$format"
        }

        holder.scoreView.text = recommendation.node?.mediaRecommendation?.meanScore.toString()
        holder.favsView.text = recommendation.node?.mediaRecommendation?.favourites.toString()
        holder.cardView.setOnClickListener {
            val mediaID = recommendation.node?.mediaRecommendation?.id
            if (recommendation.node?.mediaRecommendation?.type?.name == "ANIME" && mediaID != null){
                listenerAnime.onAnimeClick(mediaID)
            } else if (recommendation.node?.mediaRecommendation?.type?.name == "MANGA" && mediaID != null){
                listenerManga.onMangaClick(mediaID)
            }
        }
    }

    override fun getItemCount(): Int = recommendationsList.size
}
