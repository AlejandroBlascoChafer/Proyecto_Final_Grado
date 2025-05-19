package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.details.StudioAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentStaffDetailsBinding
import com.example.proyecto_final_grado.databinding.FragmentStudiosBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.utils.openMediaDetailFragment
import graphql.GetMediaDetailQuery
import graphql.GetStudioDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StudiosDetailsFragment : Fragment(), OnAnimeClickListener {

    private var _binding: FragmentStudiosBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var studioName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        studioName = arguments?.getString("NAME")

        studioName?.let { name ->
            loadStudioDetail(name)
        }
    }

    private fun loadStudioDetail(studioName: String) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetStudioDetailQuery(studioName)).execute()
                val studio = response.data?.Studio




                withContext(Dispatchers.Main){
                    binding.textStudioName.text = studio?.name
                    binding.recyclerStudioMedia.apply {
                        layoutManager = GridLayoutManager(requireContext(), 3)
                        val mediaList = studio?.media?.edges?.filterNotNull() ?: emptyList()
                        adapter = StudioAdapter(mediaList, this@StudiosDetailsFragment)
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
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

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }
}