
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

class StaffAdapter(private val staffList: List<GetDetailQuery.Edge1>) :
    RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    inner class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.staffImageView)
        val nameView: TextView = itemView.findViewById(R.id.staffNameTextView)
        val roleView: TextView = itemView.findViewById(R.id.staffRoleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        Picasso.get().load(staff.node?.image?.large).into(holder.imageView)
        holder.nameView.text = staff.node?.name?.full ?: ""
        holder.roleView.text = staff.role ?: ""
    }

    override fun getItemCount(): Int = staffList.size
}
