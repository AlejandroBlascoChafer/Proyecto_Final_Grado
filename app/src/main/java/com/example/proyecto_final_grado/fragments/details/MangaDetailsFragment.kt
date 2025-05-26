package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.adapters.details.CharactersMediaAdapter
import com.example.proyecto_final_grado.adapters.details.RelationsAdapter
import com.example.proyecto_final_grado.adapters.details.StaffAdapter
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
import com.example.proyecto_final_grado.adapters.details.ExternalLinksAdapter
import com.example.proyecto_final_grado.adapters.details.RecommendationsAdapter
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import com.google.android.material.chip.Chip
import graphql.UpdateFavouriteMutation

class MangaDetailsFragment : Fragment(), OnCharacterClickListener, OnMangaClickListener, OnAnimeClickListener, OnStaffClickListener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var mediaId: Int? = null
    private val markwon = MarkdownUtils

    private var isFavourite = false

    private val sharedViewModel: SharedViewModel by activityViewModels()

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
                    binding.nativeTitleTextView.text = media?.title?.native

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

                    binding.genresChipGroup.removeAllViews()  // Limpiar chips previos

                    media?.genres?.forEach { genre ->
                        val chip = Chip(binding.genresChipGroup.context).apply {
                            text = genre
                            isClickable = false
                            isCheckable = false
                            // Si quieres, puedes aplicar estilo o color aquí
                        }
                        binding.genresChipGroup.addView(chip)
                    }
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



                    isFavourite = media?.isFavourite == true
                    updateFavouriteButtonStyle()

                    binding.favButton.setOnClickListener {
                        updateFavourite(mediaID)
                    }

                    val mainCharacters = media?.characters?.edges?.filter { it?.role?.name == "MAIN" }

                    binding.charactersRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val characters = mainCharacters?.filterNotNull() ?: emptyList()
                        adapter = CharactersMediaAdapter(characters, this@MangaDetailsFragment)
                    }

                    val mainStaff = media?.staff?.edges
                        ?.filter { edge ->
                            val role = edge?.role?.trim()?.lowercase() ?: return@filter false
                            role == "story" || role == "art" || role == "story & art"
                        }


                    binding.staffRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val staff = mainStaff?.filterNotNull() ?: emptyList()
                        adapter = StaffAdapter(staff, this@MangaDetailsFragment)
                    }

                    binding.relationsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val relations = media?.relations?.edges?.filterNotNull() ?: emptyList()
                        adapter = RelationsAdapter(relations, this@MangaDetailsFragment, this@MangaDetailsFragment)
                    }

                    binding.recommendationsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val recommendations = media?.recommendations?.edges?.filterNotNull() ?: emptyList()
                        adapter = RecommendationsAdapter(recommendations, this@MangaDetailsFragment, this@MangaDetailsFragment)
                    }

                    binding.externalLinksRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val links = media?.externalLinks?.filterNotNull() ?: emptyList()
                        adapter = ExternalLinksAdapter(links, this@MangaDetailsFragment, this@MangaDetailsFragment)
                    }

                    binding.showMoreCharacters.setOnClickListener{
                        val allCharactersFragment = AllCharactersFragment().apply {
                            arguments = Bundle().apply {
                                putInt("MEDIA_ID", mediaID)
                            }
                        }
                        (activity as? MainActivity)?.openDetailFragment(allCharactersFragment)
                    }
                    binding.showMoreStaff.setOnClickListener{
                        val allCharactersFragment = AllStaffFragment().apply {
                            arguments = Bundle().apply {
                                putInt("MEDIA_ID", mediaID)
                            }
                        }
                        (activity as? MainActivity)?.openDetailFragment(allCharactersFragment)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Error", "Error fetching manga details: ${e.message}")
                }
            }
        }
    }

    private fun updateFavourite(mediaID: Int){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val response = apolloClient.mutation(
                    UpdateFavouriteMutation(
                    animeId = Optional.absent(),
                    mangaId = Optional.present(mediaID),
                    characterId = Optional.absent(),
                    studioId = Optional.absent(),
                    staffId = Optional.absent()
                )
                ).execute()

                val updatedEntry = response.data?.ToggleFavourite?.manga
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
            text = if (isFavourite) "REMOVE FAVOURITE" else "SET AS FAVOURITE"
            setBackgroundColor(ContextCompat.getColor(context, bgColorRes))
            setTextColor(ContextCompat.getColor(context, textColorRes))
        }
    }

    override fun onCharacterClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { CharacterDetailsFragment() }
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
