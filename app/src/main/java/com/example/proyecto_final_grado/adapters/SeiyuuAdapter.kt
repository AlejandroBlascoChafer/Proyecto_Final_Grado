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

class SeiyuuAdapter(private val mediaList: List<GetCharacterDetailQuery.VoiceActorRole?>) : RecyclerView.Adapter<SeiyuuAdapter.SeiyuuViewHolder>() {

    inner class SeiyuuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.seiyuuImageView)
        val nameView: TextView = itemView.findViewById(R.id.seiyuuNameTextView)
        val roleView: TextView = itemView.findViewById(R.id.seiyuuLanguageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeiyuuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seiyu, parent, false)
        return SeiyuuViewHolder(view)
    }

    override fun getItemCount(): Int = mediaList.size

    override fun onBindViewHolder(holder: SeiyuuViewHolder, position: Int) {
        val media = mediaList[position]
        Picasso.get().load(media?.voiceActor?.image?.large).into(holder.imageView)
        holder.nameView.text = media?.voiceActor?.name?.userPreferred ?: ""
        holder.roleView.text = media?.voiceActor?.languageV2 ?: ""
    }

}
