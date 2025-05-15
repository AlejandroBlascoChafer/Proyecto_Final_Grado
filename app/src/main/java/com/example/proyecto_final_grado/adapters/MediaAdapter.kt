package com.example.proyecto_final_grado.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.squareup.picasso.Picasso
import graphql.GetCharacterDetailQuery

class MediaAdapter(private val mediaList: List<GetCharacterDetailQuery.Edge?>) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

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
        holder.nameView.text = media?.node?.title?.userPreferred ?: ""
        holder.roleView.text = media?.node?.format?.name ?: ""
    }

}
