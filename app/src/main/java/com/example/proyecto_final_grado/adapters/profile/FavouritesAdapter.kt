package com.example.proyecto_final_grado.adapters.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemProfileFavouritesBinding
import graphql.GetUserProfileInfoQuery
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_ANIME
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_CHARACTER
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_MANGA
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_STAFF
import com.squareup.picasso.Picasso

class FavouritesAdapter(
    private val items: List<Any>,
    private val type: Int,
    private val listenerAnime: OnAnimeClickListener,
    private val listenerManga: OnMangaClickListener,
    private val listenerCharacter: OnCharacterClickListener,
    private val listenerStaff: OnStaffClickListener
) : RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder>() {

    inner class FavouritesViewHolder(val binding: ItemProfileFavouritesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
        val binding = ItemProfileFavouritesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavouritesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        val item = items[position]
        val imageUrl = when (type) {
            FAV_TYPE_ANIME -> (items[position] as GetUserProfileInfoQuery.Edge).node?.coverImage?.large
            FAV_TYPE_MANGA -> (items[position] as GetUserProfileInfoQuery.Edge1).node?.coverImage?.large
            FAV_TYPE_CHARACTER -> (items[position] as GetUserProfileInfoQuery.Edge2).node?.image?.large
            FAV_TYPE_STAFF -> (items[position] as GetUserProfileInfoQuery.Edge3).node?.image?.large
            else -> null
        }

        imageUrl?.let { Picasso.get().load(it).into(holder.binding.image) }

        holder.binding.image.setOnClickListener {
            val nodeId = when (type) {
                FAV_TYPE_ANIME -> (item as GetUserProfileInfoQuery.Edge).node?.id
                FAV_TYPE_MANGA -> (item as GetUserProfileInfoQuery.Edge1).node?.id
                FAV_TYPE_CHARACTER -> (item as GetUserProfileInfoQuery.Edge2).node?.id
                FAV_TYPE_STAFF -> (item as GetUserProfileInfoQuery.Edge3).node?.id
                else -> null
            }
            Log.d("MediaID", nodeId.toString())

            nodeId?.let {
                when (type) {
                    FAV_TYPE_ANIME -> listenerAnime.onAnimeClick(it)
                    FAV_TYPE_MANGA -> listenerManga.onMangaClick(it)
                    FAV_TYPE_CHARACTER -> listenerCharacter.onCharacterClick(it)
                    FAV_TYPE_STAFF -> listenerStaff.onStaffClick(it)
                }
            }
        }

    }

    override fun getItemCount(): Int = items.size
}
