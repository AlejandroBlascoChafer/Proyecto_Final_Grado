package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.databinding.ItemAllCharactersBinding
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class AllCharactersAdapter(
    private val allCharactersList: List<GetMediaDetailQuery.Edge?>,
    private var voiceActorLanguage: String,
    private val characterListener: OnCharacterClickListener,
    private val staffListener: OnStaffClickListener):
    RecyclerView.Adapter<AllCharactersAdapter.AllCharacterViewHolder>() {

    private lateinit var binding: ItemAllCharactersBinding

    inner class AllCharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = binding.characterImage
        val nameView: TextView = binding.characterName
        val seiyuuNameView: TextView = binding.seiyuuName
        val seiyuuImageView: ImageView = binding.seiyuuImage
        val layoutCharacter: LinearLayout = binding.layoutCharacter
        val layoutSeiyuu: LinearLayout = binding.layoutSeiyuu
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllCharactersAdapter.AllCharacterViewHolder {
        binding = ItemAllCharactersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllCharacterViewHolder(binding.root)
    }

    override fun getItemCount(): Int = allCharactersList.size

    override fun onBindViewHolder(holder: AllCharacterViewHolder, position: Int) {
        val character = allCharactersList[position]

        Picasso.get().load(character?.node?.image?.large).into(holder.imageView)
        holder.nameView.text = character?.node?.name?.userPreferred

        val voiceActor = character?.voiceActors?.firstOrNull {
            it?.languageV2.equals(voiceActorLanguage, ignoreCase = true)
        }

        if (voiceActor != null) {
            holder.layoutSeiyuu.visibility = View.VISIBLE
            holder.seiyuuNameView.text = voiceActor.name?.userPreferred

            holder.layoutSeiyuu.setOnClickListener {
                voiceActor.id.let { id ->
                    staffListener.onStaffClick(id)
                }
            }

            Picasso.get().load(voiceActor.image?.large).into(holder.seiyuuImageView)
        } else {
            holder.layoutSeiyuu.visibility = View.GONE
            holder.layoutSeiyuu.setOnClickListener(null)
        }

        holder.layoutCharacter.setOnClickListener {
            character?.node?.id?.let { id ->
                characterListener.onCharacterClick(id)
            }
        }
    }

    fun setVoiceActorLanguage(language: String) {
        if (voiceActorLanguage != language) {
            voiceActorLanguage = language
            // Actualiza solo los ítems visibles para mejorar rendimiento
            notifyItemRangeChanged(0, allCharactersList.size)
        }
    }

}