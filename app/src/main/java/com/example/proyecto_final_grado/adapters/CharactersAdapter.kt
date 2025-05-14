package com.example.proyecto_final_grado.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.squareup.picasso.Picasso
import graphql.GetDetailQuery

class CharactersAdapter(private val characters: List<GetDetailQuery.Node>) :
    RecyclerView.Adapter<CharactersAdapter.CharacterViewHolder>() {

    inner class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.characterImageView)
        val nameView: TextView = itemView.findViewById(R.id.characterNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = characters[position]
        Picasso.get().load(character.image?.large).into(holder.imageView)
        Log.d("CharacterAdapter", "Name: ${character.name?.userPreferred}")

        holder.nameView.text = character.name?.userPreferred
    }

    override fun getItemCount(): Int = characters.size
}
