package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.databinding.FragmentStaffDetailsBinding
import com.example.proyecto_final_grado.databinding.FragmentStudiosBinding


class StudiosDetailsFragment : Fragment() {

    private var _binding: FragmentStudiosBinding? = null
    private val binding get() = _binding!!

    private var studioId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        studioId = arguments?.getInt("MEDIA_ID")

        studioId?.let { id ->
            loadStudioDetail(id)
        }
    }

    private fun loadStudioDetail(studioID: Int) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudiosBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}