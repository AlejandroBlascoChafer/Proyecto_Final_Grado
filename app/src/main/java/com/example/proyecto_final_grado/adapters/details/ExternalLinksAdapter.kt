
package com.example.proyecto_final_grado.adapters.details

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.databinding.ItemLinkBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class ExternalLinksAdapter(
    private val externalLinksList: List<GetMediaDetailQuery.ExternalLink?>,
    private val listenerManga: OnMangaClickListener,
    private val listenerAnime: OnAnimeClickListener
) :
    RecyclerView.Adapter<ExternalLinksAdapter.ExternalLinksViewHolder>() {
    private lateinit var binding: ItemLinkBinding

    inner class ExternalLinksViewHolder(val binding: ItemLinkBinding) : RecyclerView.ViewHolder(binding.root) {
        val layout: LinearLayout = binding.layout
        val textView: TextView = binding.textLink
        val imageView: ImageView = binding.iconExternalLink
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExternalLinksViewHolder {
        binding = ItemLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExternalLinksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExternalLinksViewHolder, position: Int) {
        val link = externalLinksList[position]
        Picasso.get().load(link?.icon).placeholder(R.drawable.ic_link).error(R.drawable.ic_link).into(holder.imageView)
        if (link?.language == null){
            holder.textView.text = link?.site
        } else {
            holder.textView.text = link.site + " (" +link.language + ")"
        }

        val backgroundColor = try {
            if (!link?.color.isNullOrBlank()) Color.parseColor(link?.color)
            else Color.parseColor("#666666")
        } catch (e: IllegalArgumentException) {
            Color.parseColor("#666666")
        }

        holder.layout.setBackgroundColor(backgroundColor)

        holder.layout.setOnClickListener {
            val url = link?.url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = externalLinksList.size
}
