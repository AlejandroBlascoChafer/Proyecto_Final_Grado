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
import graphql.GetStaffDetailQuery

class CharactersStaffAdapter(
    private val characters: List<GetStaffDetailQuery.Edge1?>,
    private val characterListener: OnCharacterClickListener) :
    RecyclerView.Adapter<CharactersStaffAdapter.CharacterViewHolder>() {

    inner class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.characterImageView)
        val nameView: TextView = itemView.findViewById(R.id.characterNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = characters[position]

        Picasso.get().load(character?.node?.image?.large).into(holder.imageView)
        holder.nameView.text = character?.node?.name?.full

        holder.imageView.setOnClickListener {
            val characterID = character?.node?.id
            if (characterID != null) {
                characterListener.onCharacterClick(characterID)
            }
        }

    }

    override fun getItemCount(): Int = characters.size
}
