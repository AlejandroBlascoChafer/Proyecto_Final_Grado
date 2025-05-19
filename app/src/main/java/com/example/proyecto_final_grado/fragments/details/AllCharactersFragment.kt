package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.details.AllCharactersAdapter
import com.example.proyecto_final_grado.adapters.details.ExternalLinksAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentAllCharactersBinding
import com.example.proyecto_final_grado.databinding.FragmentDetailsBinding
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.utils.openMediaDetailFragment
import graphql.GetMediaDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllCharactersFragment : Fragment(), OnCharacterClickListener {

    private var _binding: FragmentAllCharactersBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var mediaID: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaID = arguments?.getInt("MEDIA_ID")
        mediaID?.let { fetchCharacters(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun fetchCharacters(mediaID:Int){
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetMediaDetailQuery(mediaID)).execute()

                withContext(Dispatchers.Main){
                    binding.allCharactersRecyclerView.apply {
                        layoutManager = GridLayoutManager(requireContext(), 2)
                        val allCharacters = response.data?.Media?.characters?.edges ?: emptyList()
                        adapter = AllCharactersAdapter(allCharacters, this@AllCharactersFragment)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Error", "Error fetching manga details: ${e.message}")
                }
            }
        }
    }

    override fun onCharacterClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { CharacterDetailsFragment() }
    }

}