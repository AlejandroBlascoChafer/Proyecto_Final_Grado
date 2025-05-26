package com.example.proyecto_final_grado.adapters.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.databinding.ItemAllCharactersBinding
import com.example.proyecto_final_grado.databinding.ItemAllStaffBinding
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery

class AllStaffAdapter(
    private val allStaffList: List<GetMediaDetailQuery.Edge2?>,
    private val staffListener: OnStaffClickListener
) : RecyclerView.Adapter<AllStaffAdapter.AllStaffViewHolder>() {

    private lateinit var binding: ItemAllStaffBinding

    inner class AllStaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = binding.staffImage
        val nameView: TextView = binding.staffName
        val roleView: TextView = binding.staffRole
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllStaffViewHolder {
        binding = ItemAllStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllStaffViewHolder(binding.root)
    }

    override fun getItemCount(): Int = allStaffList.size

    override fun onBindViewHolder(holder: AllStaffViewHolder, position: Int) {
        val staff = allStaffList[position]

        Picasso.get().load(staff?.node?.image?.large).into(holder.imageView)
        holder.nameView.text = staff?.node?.name?.full
        holder.roleView.text = staff?.role

        holder.imageView.setOnClickListener {
            val staffID = staff?.node?.id
            if (staffID != null) {
                staffListener.onStaffClick(staffID)
            }
        }
    }
}
