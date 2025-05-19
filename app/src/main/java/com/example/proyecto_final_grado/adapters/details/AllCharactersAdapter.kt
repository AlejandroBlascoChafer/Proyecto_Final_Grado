package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.databinding.ItemAllCharactersBinding
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class AllCharactersAdapter(
    private val allCharactersList: List<GetMediaDetailQuery.Edge?>,
    private val characterListener: OnCharacterClickListener):
    RecyclerView.Adapter<AllCharactersAdapter.AllCharacterViewHolder>() {

        private lateinit var binding: ItemAllCharactersBinding


    inner class AllCharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = binding.characterImage
        val nameView: TextView = binding.characterName
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllCharactersAdapter.AllCharacterViewHolder {
        binding = ItemAllCharactersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllCharacterViewHolder(binding.root)
    }

    override fun getItemCount(): Int = allCharactersList.size

    override fun onBindViewHolder(holder: AllCharactersAdapter.AllCharacterViewHolder, position: Int) {
        val character = allCharactersList[position]

        Picasso.get().load(character?.node?.image?.large).into(holder.imageView)
        holder.nameView.text = character?.node?.name?.userPreferred

        holder.imageView.setOnClickListener {
            val characterID = character?.node?.id
            if (characterID != null) {
                characterListener.onCharacterClick(characterID)
            }
        }
    }
}