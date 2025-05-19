package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.databinding.ItemCharacterBinding
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class CharactersMediaAdapter(
    private val characters: List<GetMediaDetailQuery.Edge>,
    private val characterListener: OnCharacterClickListener) :
    RecyclerView.Adapter<CharactersMediaAdapter.CharacterViewHolder>() {

        private lateinit var binding: ItemCharacterBinding

    inner class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = binding.characterImageView
        val nameView: TextView = binding.characterNameTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = characters[position]

        Picasso.get().load(character.node?.image?.large).into(holder.imageView)
        holder.nameView.text = character.node?.name?.userPreferred

        holder.imageView.setOnClickListener {
            val characterID = character.node?.id
            if (characterID != null) {
                characterListener.onCharacterClick(characterID)
            }
        }

    }

    override fun getItemCount(): Int = characters.size
}
