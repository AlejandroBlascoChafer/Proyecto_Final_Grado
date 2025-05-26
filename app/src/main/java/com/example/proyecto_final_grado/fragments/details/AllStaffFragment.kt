package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.adapters.details.AllStaffAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentAllStaffBinding
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import graphql.GetMediaDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllStaffFragment : Fragment(), OnStaffClickListener {

    private var _binding: FragmentAllStaffBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var mediaID: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaID = arguments?.getInt("MEDIA_ID")
        mediaID?.let { fetchStaff(it) }
    }

    private fun fetchStaff(mediaID: Int) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetMediaDetailQuery(mediaID)).execute()
                withContext(Dispatchers.Main) {
                    binding.allStaffRecyclerView.apply {
                        layoutManager = GridLayoutManager(requireContext(), 2)
                        val allStaff = response.data?.Media?.staff?.edges ?: emptyList()
                        adapter = AllStaffAdapter(allStaff, this@AllStaffFragment)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Error", "Error fetching staff details: ${e.message}")
                }
            }
        }
    }

    override fun onStaffClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { StaffDetailsFragment() }
    }
}
