package com.example.proyecto_final_grado.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import graphql.GetUserProfileInfoQuery
import com.example.proyecto_final_grado.databinding.ItemProfileLikeBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.squareup.picasso.Picasso

const val FAV_TYPE_ANIME = 0
const val FAV_TYPE_MANGA = 1
const val FAV_TYPE_CHARACTER = 2
const val FAV_TYPE_STAFF = 3

class LikesAdapter(
    private val items: List<Any>,
    private val type: Int,
    private val listenerAnime: OnAnimeClickListener,
    private val listenerManga: OnMangaClickListener,
    private val listenerCharacter: OnCharacterClickListener,
    private val listenerStaff: OnStaffClickListener
) : RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    inner class LikesViewHolder(val binding: ItemProfileLikeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val binding = ItemProfileLikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
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
