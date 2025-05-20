package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.details.StudioAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentStaffDetailsBinding
import com.example.proyecto_final_grado.databinding.FragmentStudiosBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.utils.SharedViewModel
import com.example.proyecto_final_grado.utils.openMediaDetailFragment
import graphql.GetMediaDetailQuery
import graphql.GetStudioDetailQuery
import graphql.UpdateFavouriteMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StudiosDetailsFragment : Fragment(), OnAnimeClickListener {

    private var _binding: FragmentStudiosBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var studioName: String? = null

    private var isFavourite = false

    private val sharedViewModel: SharedViewModel by activityViewModels()

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
                val studioID = studio?.id

                isFavourite = studio?.isFavourite == true
                updateFavouriteButtonStyle()
                binding.favButton.setOnClickListener {
                    if (studioID != null) {
                        updateFavourite(studioID)
                    }
                }


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

    private fun updateFavourite(studioID: Int){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val response = apolloClient.mutation(
                    UpdateFavouriteMutation(
                    animeId = Optional.absent(),
                    mangaId = Optional.absent(),
                    characterId = Optional.absent(),
                    studioId = Optional.present(studioID),
                    staffId = Optional.absent()
                )
                ).execute()

                val updatedEntry = response.data?.ToggleFavourite?.anime
                if (updatedEntry != null) {
                    withContext(Dispatchers.Main) {
                        sharedViewModel.loadInitialData()
                        isFavourite = !isFavourite
                        updateFavouriteButtonStyle()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }
        }
    }

    private fun updateFavouriteButtonStyle() {
        val context = binding.favButton.context

        val bgColorRes = if (isFavourite) R.color.anitrack_fav_added_bg else R.color.anitrack_blue
        val textColorRes = if (isFavourite) R.color.anitrack_fav_added_text else R.color.anitrack_white

        binding.favButton.apply {
            setBackgroundColor(ContextCompat.getColor(context, bgColorRes))
            setTextColor(ContextCompat.getColor(context, textColorRes))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAnimeClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { AnimeDetailsFragment() }
    }
}