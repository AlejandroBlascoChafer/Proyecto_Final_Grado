package com.example.proyecto_final_grado.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Proyecto_Final_Grado.queries.GetUserMangaListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaBinding
import com.squareup.picasso.Picasso

class MangaAdapter(private var mangaList: List<GetUserMangaListQuery.Entry>) :
    RecyclerView.Adapter<MangaAdapter.MangaViewHolder>() {

    inner class MangaViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MangaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
        val manga = mangaList[position]
        val media = manga.media

        holder.binding.tvTitle.text = media?.title?.romaji ?: "Sin título"
        holder.binding.tvScore.text = "Puntuación: ${manga.score}"
        holder.binding.tvStatus.text = manga.status?.name
        holder.binding.tvProgress.text = "Progreso: ${manga.progress ?: 0}"

        val imageUrl = media?.coverImage?.large
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(holder.binding.ivCover)
        }
    }

    override fun getItemCount(): Int = mangaList.size

    fun submitList(newList: List<GetUserMangaListQuery.Entry>) {
        mangaList = newList
        notifyDataSetChanged()
    }
}
