
package com.example.proyecto_final_grado.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.squareup.picasso.Picasso
import graphql.GetDetailQuery

class RelationsAdapter(private val relationsList: List<GetDetailQuery.Edge2>) :
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
    }

    override fun getItemCount(): Int = relationsList.size
}
