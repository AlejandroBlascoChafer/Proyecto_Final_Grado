package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.details.CharactersMediaAdapter
import com.example.proyecto_final_grado.adapters.details.CharactersStaffAdapter
import com.example.proyecto_final_grado.adapters.details.MediaCharacterAdapter
import com.example.proyecto_final_grado.adapters.details.MediaStaffAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentStaffDetailsBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import graphql.GetStaffDetailQuery
import kotlinx.coroutines.launch

class StaffDetailsFragment : Fragment(), OnMangaClickListener, OnAnimeClickListener, OnCharacterClickListener {

    private var _binding: FragmentStaffDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var characterId: Int? = null
    private val markwon = MarkdownUtils

    private lateinit var staffMediaCharacterAdapter: MediaCharacterAdapter
    private lateinit var characterAdapter: CharactersMediaAdapter
    private lateinit var characterMediaCharacterAdapter: MediaCharacterAdapter

    //private val args: StaffDetailFragmentArgs by navArgs() // Asume que usás SafeArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        characterId = arguments?.getInt("MEDIA_ID")

        characterId?.let { id ->
            loadStaffDetail(id)
        }

    }

    private fun loadStaffDetail(mediaID: Int) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        lifecycleScope.launch {
            try {
                val response = apolloClient.query(GetStaffDetailQuery(mediaID)).execute()

                val staff = response.data?.Staff
                if (staff != null){

                    Picasso.get().load(staff.image?.large).into(binding.imageStaff)

                    binding.textStaffName.text = staff.name?.full ?: ""
                    binding.textNativeName.text = staff.name?.native ?: ""

                    binding.chipGroupOccupations.removeAllViews()
                    staff.primaryOccupations?.forEach { occupation ->
                        val chip = Chip(requireContext()).apply {
                            text = occupation
                            isClickable = false
                            isCheckable = false
                        }
                        binding.chipGroupOccupations.addView(chip)
                    }

                    val details = buildList {
                        staff.age?.let { add("Age: $it") }
                        staff.gender?.let { add("Genre: $it") }
                        staff.homeTown?.let { add("Hometown: $it") }
                    }.joinToString("\n")
                    binding.textStaffDetails.text = details
                    binding.textStaffDetails.visibility = if (staff.age != null) View.VISIBLE else View.GONE

                    val showMoreButton = binding.showMoreButton
                    markwon.setMarkdownText(requireContext(), binding.textStaffDescription, staff.description)
                    var isExpanded = false
                    showMoreButton.setOnClickListener {
                        if (isExpanded) {
                            binding.textStaffDescription.maxLines = 8
                            showMoreButton.setImageResource(R.drawable.ic_arrow_down)
                        } else {
                            binding.textStaffDescription.maxLines = Integer.MAX_VALUE
                            showMoreButton.setImageResource(R.drawable.ic_arrow_up)
                        }
                        isExpanded = !isExpanded
                    }

                    binding.recyclerStaffMedia.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val staffMedia = staff.staffMedia?.edges?.filterNotNull() ?: emptyList()
                        adapter = MediaStaffAdapter(staffMedia, this@StaffDetailsFragment, this@StaffDetailsFragment)
                    }

                    binding.recyclerCharacters.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val characterMedia = staff.characters?.edges ?: emptyList()
                        adapter = CharactersStaffAdapter(characterMedia, this@StaffDetailsFragment)
                        if (characterMedia.isNotEmpty()) {
                            binding.titleCharacters.visibility = View.VISIBLE
                            binding.recyclerCharacters.visibility = View.VISIBLE
                            binding.separatorCharacters.visibility = View.VISIBLE
                        }
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

    override fun onAnimeClick(mediaID: Int) {
        val animeDetailFragment = AnimeDetailsFragment().apply {
            // Pasar el ID del anime al fragmento de detalle usando un Bundle
            arguments = Bundle().apply {
                putInt("MEDIA_ID", mediaID)
            }
        }

        // Iniciar la transacción del fragmento
        (activity as? MainActivity)?.openDetailFragment(animeDetailFragment)
    }

    override fun onMangaClick(mediaID: Int) {
        val mangaDetailFragment = MangaDetailsFragment().apply {
            // Pasar el ID del anime al fragmento de detalle usando un Bundle
            arguments = Bundle().apply {
                putInt("MEDIA_ID", mediaID)
            }
        }

        // Iniciar la transacción del fragmento
        (activity as? MainActivity)?.openDetailFragment(mangaDetailFragment)
    }

    override fun onCharacterClick(mediaID: Int) {
        val characterDetailsFragment = CharacterDetailsFragment().apply {
            // Pasar el ID del anime al fragmento de detalle usando un Bundle
            arguments = Bundle().apply {
                putInt("MEDIA_ID", mediaID)
            }
        }

        // Iniciar la transacción del fragmento
        (activity as? MainActivity)?.openDetailFragment(characterDetailsFragment)
    }

}
