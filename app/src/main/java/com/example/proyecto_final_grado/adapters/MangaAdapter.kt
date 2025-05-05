package com.example.proyecto_final_grado.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Proyecto_Final_Grado.queries.GetUserMangaListQuery
import com.example.proyecto_final_grado.databinding.ItemMediaBinding
import com.squareup.picasso.Picasso

class MangaAdapter(
    private var mangaList: List<GetUserMangaListQuery.Entry>
) : RecyclerView.Adapter<MangaAdapter.MangaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MangaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
        holder.bind(mangaList[position])
    }

    override fun getItemCount(): Int = mangaList.size

    fun submitList(newList: List<GetUserMangaListQuery.Entry>) {
        mangaList = newList
        this.notifyDataSetChanged()
    }

    inner class MangaViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(manga: GetUserMangaListQuery.Entry) {
            val chaptersRead = manga.progress ?: 0
            val totalChapters = manga.media?.chapters
            val volumesRead = manga.media?.volumes ?: 0
            val totalVolumes = manga.media?.volumes ?: "?"
            val volumeText = "$volumesRead / $totalVolumes volumes"

            binding.tvProgress.text = if (totalChapters != null) {
                "$chaptersRead / $totalChapters capítulos"
            } else {
                "$chaptersRead / ? capítulos"
            }

            val percentage = if (totalChapters != null && totalChapters > 0) {
                chaptersRead * 100 / totalChapters
            } else {
                0
            }
            binding.progressBar.progress = percentage

            binding.tvTitle.text = manga.media?.title?.userPreferred ?: "No title"
            binding.tvScore.text = manga.score?.toString() ?: "No score"
            binding.tvVolumes.text = volumeText
            binding.tvMedia.text = manga.media?.format?.name

            Picasso.get()
                .load(manga.media?.coverImage?.large)
                .fit()
                .centerCrop()
                .into(binding.ivCover)

            binding.volumeLayout.visibility = android.view.View.VISIBLE
            binding.btnAddEpisode.text = "+1 CH"
        }
    }
}
