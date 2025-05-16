package com.example.proyecto_final_grado.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.squareup.picasso.Picasso
import graphql.GetCharacterDetailQuery
import graphql.GetStaffDetailQuery

class MediaStaffAdapter(
    private val mediaList: List<GetStaffDetailQuery.Edge?>,
    private val listenerManga: OnMangaClickListener,
    private val listenerAnime: OnAnimeClickListener
    ) : RecyclerView.Adapter<MediaStaffAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.mediaImageView)
        val nameView: TextView = itemView.findViewById(R.id.mediaNameTextView)
        val roleView: TextView = itemView.findViewById(R.id.mediaTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        Picasso.get().load(media?.node?.coverImage?.large).into(holder.imageView)
        holder.nameView.text = media?.node?.title?.romaji ?: ""
        holder.roleView.text = media?.staffRole ?: ""
        holder.imageView.setOnClickListener {
            val mediaID = media?.node?.id
            if (media?.node?.type?.name == "ANIME" && mediaID != null){
                listenerAnime.onAnimeClick(mediaID)
            } else if (media?.node?.type?.name == "MANGA" && mediaID != null){
                listenerManga.onMangaClick(mediaID)
            }
        }
    }

}
