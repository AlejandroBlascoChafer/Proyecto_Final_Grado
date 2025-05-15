
package com.example.proyecto_final_grado.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class RelationsAdapter(
    private val relationsList: List<GetMediaDetailQuery.Edge3>,
    private val listenerManga: OnMangaClickListener,
    private val listenerAnime: OnAnimeClickListener
) :
    RecyclerView.Adapter<RelationsAdapter.RelationsViewHolder>() {

    inner class RelationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.relationsImageView)
        val nameView: TextView = itemView.findViewById(R.id.relationsNameTextView)
        val roleView: TextView = itemView.findViewById(R.id.relationsTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_relations, parent, false)
        return RelationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RelationsViewHolder, position: Int) {
        val relations = relationsList[position]
        Picasso.get().load(relations.node?.coverImage?.large).into(holder.imageView)
        holder.nameView.text = relations.node?.title?.userPreferred ?: ""
        holder.roleView.text = relations.relationType?.name ?: ""
        holder.imageView.setOnClickListener {
            val mediaID = relations.node?.id
            if (relations.node?.type?.name == "ANIME" && mediaID != null){
                listenerAnime.onAnimeClick(mediaID)
            } else if (relations.node?.type?.name == "MANGA" && mediaID != null){
                listenerManga.onMangaClick(mediaID)
            }
        }
    }

    override fun getItemCount(): Int = relationsList.size
}
