package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.details.MediaCharacterAdapter
import com.example.proyecto_final_grado.adapters.details.SeiyuuAdapter
import graphql.GetCharacterDetailQuery
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentCharacterDetailsBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import com.squareup.picasso.Picasso
import graphql.UpdateFavouriteMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CharacterDetailsFragment : Fragment(), OnAnimeClickListener, OnMangaClickListener, OnStaffClickListener {

    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var characterId: Int? = null
    private val markwon = MarkdownUtils

    private var isFavourite = false

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        characterId = arguments?.getInt("MEDIA_ID")

        characterId?.let { id ->
            loadCharacterDetail(id)
        }
    }

    private fun loadCharacterDetail(characterId: Int) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        lifecycleScope.launch {
            try {
                val response = apolloClient.query(GetCharacterDetailQuery(characterId)).execute()

                val character = response.data?.Character
                if (character != null) {
                    binding.characterNameTextView.text = character.name?.userPreferred.orEmpty()
                    binding.characterNativeNameTextView.text = character.name?.native.orEmpty()
                    binding.characterAlternativeNamesTextView.text =
                        character.name?.alternative.orEmpty().toString()
                    binding.characterGenderTextView.text = "Gender: ${character.gender ?: "Desconocido"}"
                    binding.characterFavouritesTextView.text = "Favourites: ${character.favourites ?: 0}"
                    val showMoreButton = binding.showMoreButton
                    markwon.setMarkdownText(requireContext(), binding.characterDescriptionTextView, character.description)

                    isFavourite = character.isFavourite == true
                    updateFavouriteButtonStyle()

                    var isExpanded = false
                    showMoreButton.setOnClickListener {
                        if (isExpanded) {
                            binding.characterDescriptionTextView.maxLines = 5
                            showMoreButton.setImageResource(R.drawable.ic_arrow_down)
                        } else {
                            binding.characterDescriptionTextView.maxLines = Integer.MAX_VALUE
                            showMoreButton.setImageResource(R.drawable.ic_arrow_up)
                        }
                        isExpanded = !isExpanded
                    }

                    Picasso.get()
                        .load(character.image?.large)
                        .into(binding.characterBannerImageView)

                    binding.favButton.setOnClickListener {
                        updateFavourite(characterId)
                    }

                    binding.characterMediaRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val media = character.media?.edges?.filterNotNull() ?: emptyList()
                        adapter = MediaCharacterAdapter(media, this@CharacterDetailsFragment, this@CharacterDetailsFragment)
                    }
                    binding.characterSeiyuuRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val seiyuu = character.media?.edges
                            ?.flatMap { it?.voiceActorRoles ?: emptyList() }
                            ?.distinctBy { it?.voiceActor?.id }
                            ?: emptyList()

                        adapter = SeiyuuAdapter(seiyuu, this@CharacterDetailsFragment)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Manejo de errores (opcional: mostrar un mensaje o una vista vacía)
            }
        }
    }

    private fun updateFavourite(mediaID: Int){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val response = apolloClient.mutation(
                    UpdateFavouriteMutation(
                    animeId = Optional.absent(),
                    mangaId = Optional.absent(),
                    characterId = Optional.present(mediaID),
                    studioId = Optional.absent(),
                    staffId = Optional.absent()
                )
                ).execute()

                val updatedEntry = response.data?.ToggleFavourite?.characters
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

    override fun onMangaClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { MangaDetailsFragment() }
    }

    override fun onStaffClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { StaffDetailsFragment() }
    }
}
