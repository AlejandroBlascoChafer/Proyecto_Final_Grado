package com.example.proyecto_final_grado.adapters.homeAdapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemSearchBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.listeners.OnStudioClickListener
import com.example.proyecto_final_grado.models.SearchItem
import com.squareup.picasso.Picasso

class SearchAdapter(private var list: List<SearchItem>,
                    private val animeClickListener: OnAnimeClickListener,
                    private val mangaClickListener: OnMangaClickListener,
                    private val characterClickListener: OnCharacterClickListener,
                    private val staffClickListener: OnStaffClickListener,
                    private val studioClickListener: OnStudioClickListener
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    fun submitList(newList: List<SearchItem>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem) {
            when (item) {
                is SearchItem.AnimeMangaItem -> {
                    binding.tvTitle.text = item.title
                    binding.tvScore.text = item.meanScore?.toString() ?: "No score"
                    binding.tvFavs.text = "${item.favourites}"

                    Picasso.get().load(item.imageUrl).into(binding.ivCover)

                    binding.tvYearRelease.visibility = View.GONE
                    binding.root.setOnClickListener{
                        if (item.type == "ANIME" ){
                            animeClickListener.onAnimeClick(item.id)
                        } else if (item.type == "MANGA" ){
                            mangaClickListener.onMangaClick(item.id)
                        }
                    }
                }
                is SearchItem.CharacterItem -> {
                    binding.tvTitle.text = item.name
                    binding.tvScore.visibility = View.GONE
                    binding.tvFavs.text = "${item.favourites}"

                    Picasso.get().load(item.imageUrl).into(binding.ivCover)

                    binding.tvYearRelease.visibility = View.GONE
                    binding.root.setOnClickListener{
                        characterClickListener.onCharacterClick(item.id)
                    }
                }
                is SearchItem.StaffItem -> {
                    binding.tvTitle.text = item.name
                    binding.tvScore.visibility = View.GONE
                    binding.tvFavs.text = "${item.favourites}"

                    Picasso.get().load(item.imageUrl).into(binding.ivCover)

                    binding.tvYearRelease.visibility = View.GONE
                    Log.d("Studio", item.name)
                    binding.root.setOnClickListener{
                        staffClickListener.onStaffClick(item.id)
                    }
                }
                is SearchItem.StudioItem -> {
                    binding.tvTitle.text = item.name
                    binding.tvScore.visibility = View.GONE
                    binding.tvFavs.text = "${item.favourites}"

                    Picasso.get().load(item.imageUrl).into(binding.ivCover)

                    binding.tvYearRelease.visibility = View.GONE
                    binding.root.setOnClickListener{
                        studioClickListener.onStudioClick(item.name)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }
}
