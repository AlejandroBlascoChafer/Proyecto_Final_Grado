package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.core.animate
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.proyecto_final_grado.adapters.details.CharactersMediaAdapter.CharacterViewHolder
import com.example.proyecto_final_grado.databinding.FragmentStudiosBinding
import com.example.proyecto_final_grado.databinding.ItemCharacterBinding
import com.example.proyecto_final_grado.databinding.ItemMediaBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.squareup.picasso.Picasso
import graphql.GetStudioDetailQuery

class StudioAdapter(
    private val mediaList: List<GetStudioDetailQuery.Edge>,
    private val characterListener: OnAnimeClickListener) :
    RecyclerView.Adapter<StudioAdapter.StudioViewHolder>() {

        private lateinit var binding: ItemMediaBinding

    inner class StudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = binding.mediaImageView
        val nameView: TextView = binding.mediaNameTextView
        val formatView: TextView = binding.mediaTypeTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudioViewHolder {
        binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudioViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: StudioViewHolder, position: Int) {
        val anime = mediaList[position]

        Picasso.get().load(anime.node?.coverImage?.large).into(holder.imageView)
        holder.nameView.text = anime.node?.title?.userPreferred
        holder.formatView.text = anime.node?.format?.name

        holder.imageView.setOnClickListener {
            val animeID = anime.node?.id
            if (animeID != null) {
                characterListener.onAnimeClick(animeID)
            }
        }
    }


    override fun getItemCount(): Int = mediaList.size
}