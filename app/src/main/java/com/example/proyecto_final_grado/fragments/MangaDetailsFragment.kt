package com.example.proyecto_final_grado.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.CharactersMediaAdapter
import com.example.proyecto_final_grado.adapters.RelationsAdapter
import com.example.proyecto_final_grado.adapters.StaffAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentDetailsBinding
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils

class MangaDetailsFragment : Fragment(), OnCharacterClickListener, OnMangaClickListener, OnAnimeClickListener, OnStaffClickListener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var mediaId: Int? = null
    private val markwon = MarkdownUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaId = arguments?.getInt("MEDIA_ID")
        mediaId?.let { fetchMangaDetails(it) }
    }

    private fun fetchMangaDetails(mediaID: Int) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetMediaDetailQuery(mediaID)).execute()
                val media = response.data?.Media

                withContext(Dispatchers.Main) {
                    Picasso.get().load(media?.bannerImage).into(binding.bannerImageView)
                    Picasso.get().load(media?.coverImage?.large).into(binding.coverImageView)

                    binding.titleTextView.text = media?.title?.userPreferred
                    val showMoreButton = binding.showMoreButton
                    markwon.setMarkdownText(requireContext(), binding.descriptionTextView, media?.description)

                    var isExpanded = false
                    showMoreButton.setOnClickListener {
                        if (isExpanded) {
                            binding.descriptionTextView.maxLines = 3
                            showMoreButton.setImageResource(R.drawable.ic_arrow_down)
                        } else {
                            binding.descriptionTextView.maxLines = Integer.MAX_VALUE
                            showMoreButton.setImageResource(R.drawable.ic_arrow_up)
                        }
                        isExpanded = !isExpanded
                    }

                    binding.genresTextView.text = media?.genres.toString()
                    val scorePopFav = "Score: ${media?.averageScore} | Pop: ${media?.popularity} | Favs: ${media?.favourites}"
                    binding.scoreTextView.text = scorePopFav
                    val formatStatusSource = "${media?.format} | ${media?.status} | ${media?.source}"
                    binding.formatTextView.text = formatStatusSource


                    val start = media?.startDate
                    val end = media?.endDate

                    val startDate = if (start?.day != null && start.month != null && start.year != null) {
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", start.day, start.month, start.year)
                    } else {
                        "Unknown"
                    }
                    val endDate = if (end?.day != null && end.month != null && end.year != null) {
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", end.day, end.month, end.year)
                    } else {
                        "Ongoing"
                    }
                    binding.dateTextView.text = "Published: $startDate to $endDate"


                    binding.studiosTextView.visibility = View.GONE
                    binding.producersTextView.visibility = View.GONE

                    val synonymsList = media?.synonyms?.filterNotNull().orEmpty()
                    binding.synonymsTextView.text = "Synonyms:\n${synonymsList.joinToString("\n")}"

                    val tagsList = media?.tags?.mapNotNull { tag ->
                        val name = tag?.name
                        val rank = tag?.rank
                        if (!name.isNullOrBlank() && rank != null) "$name - ${rank}%" else null
                    }.orEmpty()

                    val showMoreButtonTags = binding.showMoreButtonTags
                    binding.tagsTextView.text = tagsList.joinToString("\n")

                    var isTagsExpanded = false

                    showMoreButtonTags.setOnClickListener {
                        if (isTagsExpanded) {
                            binding.tagsTextView.maxLines = 5
                            showMoreButtonTags.setImageResource(R.drawable.ic_arrow_down)
                        } else {
                            binding.tagsTextView.maxLines = Integer.MAX_VALUE
                            showMoreButtonTags.setImageResource(R.drawable.ic_arrow_up)
                        }

                        isTagsExpanded = !isTagsExpanded
                    }
                    binding.studiosTextView.visibility = View.GONE
                    binding.producersTextView.visibility = View.GONE
                    binding.separatorStudios.visibility = View.GONE
                    binding.openingsTextView.visibility = View.GONE
                    binding.endingsTextView.visibility = View.GONE
                    binding.separatorThemes.visibility = View.GONE

                    val mainCharacters = media?.characters?.edges?.filter { it?.role?.name == "MAIN" }
                    val suppCharacters = media?.characters?.edges?.filter { it?.role?.name == "SUPPORTING" }
                    val backCharacters = media?.characters?.edges?.filter { it?.role?.name == "BACKGROUND" }

                    binding.charactersRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        adapter = CharactersMediaAdapter(mainCharacters?.filterNotNull() ?: emptyList(), this@MangaDetailsFragment)
                    }

                    binding.staffRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        adapter = StaffAdapter(media?.staff?.edges?.filterNotNull() ?: emptyList(), this@MangaDetailsFragment)
                    }

                    binding.relationsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        adapter = RelationsAdapter(media?.relations?.edges?.filterNotNull() ?: emptyList(), this@MangaDetailsFragment, this@MangaDetailsFragment)
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
        val characterDetailsFragment = CharacterDetailsFragment().apply {
            // Pasar el ID del anime al fragmento de detalle usando un Bundle
            arguments = Bundle().apply {
                putInt("MEDIA_ID", mediaID)
            }
        }

        // Iniciar la transacción del fragmento
        (activity as? MainActivity)?.openDetailFragment(characterDetailsFragment)
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

    override fun onStaffClick(mediaID: Int) {
        val staffDetailFragment = StaffDetailsFragment().apply {
            // Pasar el ID del anime al fragmento de detalle usando un Bundle
            arguments = Bundle().apply {
                putInt("MEDIA_ID", mediaID)
            }
        }

        // Iniciar la transacción del fragmento
        (activity as? MainActivity)?.openDetailFragment(staffDetailFragment)
    }
}
