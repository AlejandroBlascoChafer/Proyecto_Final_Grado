package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.squareup.picasso.Picasso
import graphql.GetCharacterDetailQuery

class SeiyuuAdapter(
    private val mediaList: List<GetCharacterDetailQuery.VoiceActorRole?>,
    private val listenerStaff: OnStaffClickListener
) : RecyclerView.Adapter<SeiyuuAdapter.SeiyuuViewHolder>() {

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
        holder.imageView.setOnClickListener {
            val mediaID = media?.voiceActor?.id
            if (mediaID != null){
                listenerStaff.onStaffClick(mediaID)
            }
        }
    }

}
