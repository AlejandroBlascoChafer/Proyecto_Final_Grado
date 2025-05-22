package com.example.proyecto_final_grado.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.proyecto_final_grado.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchBottomSheet : BottomSheetDialogFragment() {



    interface OnCategorySelectedListener {
        fun onCategorySelected(category: String)
    }

    var listener: OnCategorySelectedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_search, container, false)

        view.findViewById<View>(R.id.optionAnime).setOnClickListener {
            listener?.onCategorySelected("anime")
            dismiss()
        }

        view.findViewById<View>(R.id.optionManga).setOnClickListener {
            listener?.onCategorySelected("manga")
            dismiss()
        }

        view.findViewById<View>(R.id.optionCharacters).setOnClickListener {
            listener?.onCategorySelected("characters")
            dismiss()
        }

        view.findViewById<View>(R.id.optionStaff).setOnClickListener {
            listener?.onCategorySelected("staff")
            dismiss()
        }

        view.findViewById<View>(R.id.optionStudios).setOnClickListener {
            listener?.onCategorySelected("studios")
            dismiss()
        }

        return view
    }
}
