package com.example.proyecto_final_grado.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import graphql.GetUserProfileInfoQuery
import com.example.proyecto_final_grado.databinding.ItemProfileLikeBinding
import com.squareup.picasso.Picasso

const val FAV_TYPE_ANIME = 0
const val FAV_TYPE_MANGA = 1
const val FAV_TYPE_CHARACTER = 2
const val FAV_TYPE_STAFF = 3

class LikesAdapter(
    private val items: List<Any>,
    private val type: Int
) : RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    inner class LikesViewHolder(val binding: ItemProfileLikeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val binding = ItemProfileLikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        val imageUrl = when (type) {
            FAV_TYPE_ANIME -> (items[position] as GetUserProfileInfoQuery.Node).coverImage?.large
            FAV_TYPE_MANGA -> (items[position] as GetUserProfileInfoQuery.Node1).coverImage?.large
            FAV_TYPE_CHARACTER -> (items[position] as GetUserProfileInfoQuery.Node2).image?.large
            FAV_TYPE_STAFF -> (items[position] as GetUserProfileInfoQuery.Node3).image?.large
            else -> null
        }

        imageUrl?.let { Picasso.get().load(it).into(holder.binding.image) }
    }

    override fun getItemCount(): Int = items.size
}
