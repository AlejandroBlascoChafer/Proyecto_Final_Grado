package com.example.proyecto_final_grado.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemTrendingAnimeBinding
import com.example.proyecto_final_grado.databinding.ItemTrendingMangaBinding
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.squareup.picasso.Picasso
import graphql.GetTrendingAnimeQuery
import graphql.GetTrendingMangaQuery

class MangaTrendAdapter(
    private var mangaList: List<GetTrendingMangaQuery.Medium?>,
    private val listenerManga: OnMangaClickListener,
    private val context: Context
) :
    RecyclerView.Adapter<MangaTrendAdapter.MangaTrendViewHolder>() {

    private lateinit var binding: ItemTrendingMangaBinding
    private val markwon = MarkdownUtils

    inner class MangaTrendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val banner: ImageView = binding.imageBanner
        val cover: ImageView = binding.imageCover
        val title: TextView = binding.textTitle
        val author: TextView = binding.textAuthor
        val genres: TextView = binding.textGenres
        val description: TextView = binding.textDescription
        val score : TextView = binding.textScore
        val favs : TextView = binding.textFavourites

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaTrendAdapter.MangaTrendViewHolder {
        binding = ItemTrendingMangaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MangaTrendViewHolder(binding.root)
    }

    override fun getItemCount(): Int = mangaList.size

    override fun onBindViewHolder(holder: MangaTrendAdapter.MangaTrendViewHolder, position: Int) {
        val manga = mangaList[position]

        Picasso.get().load(manga?.bannerImage).into(holder.banner)
        Picasso.get().load(manga?.coverImage?.large).into(holder.cover)

        holder.title.text = manga?.title?.userPreferred

        val validRoles = listOf("Story", "Art", "Story and Art", "Story & Art")

        val authors = manga?.staff?.edges
            ?.filter { edge ->
                val role = edge?.role?.trim()
                role != null && validRoles.any { valid -> role.contains(valid, ignoreCase = true) }
            }
            ?.mapNotNull { it?.node?.name?.userPreferred }
            ?.distinct()
            ?.joinToString(", ")

        holder.author.text = authors

        holder.genres.text = manga?.genres.toString()
        markwon.setMarkdownText(context, holder.description, manga?.description)
        holder.score.text = manga?.meanScore.toString()
        holder.favs.text = manga?.favourites.toString()

        binding.container.setOnClickListener{
            val mediaID = manga?.id
            if (mediaID != null) {
                listenerManga.onMangaClick(mediaID)
            }
        }
    }

    fun updateList(newList: List<GetTrendingMangaQuery.Medium?>) {
        mangaList = newList
        notifyDataSetChanged()
    }

}
