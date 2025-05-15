package com.example.proyecto_final_grado.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.adapters.MediaAdapter
import com.example.proyecto_final_grado.adapters.SeiyuuAdapter
import com.example.proyecto_final_grado.adapters.StaffAdapter
import graphql.GetCharacterDetailQuery
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentCharacterDetailsBinding
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class CharacterDetailFragment : Fragment() {

    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var characterId: Int? = null
    private val markwon = MarkdownUtils

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
                    markwon.setMarkdownText(requireContext(), binding.characterDescriptionTextView, character.description)

                    Picasso.get()
                        .load(character.image?.large)
                        .into(binding.characterBannerImageView)

                    // Puedes implementar aquí los RecyclerViews para media/roles si lo deseas
                    binding.characterMediaRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val media = character.media?.edges?.filterNotNull() ?: emptyList()
                        adapter = MediaAdapter(media)
                    }
                    binding.characterSeiyuuRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val seiyuu = character.media?.edges
                            ?.flatMap { it?.voiceActorRoles ?: emptyList() }
                            ?.distinctBy { it?.voiceActor?.id }
                            ?: emptyList()

                        adapter = SeiyuuAdapter(seiyuu)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Manejo de errores (opcional: mostrar un mensaje o una vista vacía)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
