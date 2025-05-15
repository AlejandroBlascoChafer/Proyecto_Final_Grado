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
import com.example.proyecto_final_grado.adapters.CharactersAdapter
import com.example.proyecto_final_grado.adapters.RelationsAdapter
import com.example.proyecto_final_grado.adapters.StaffAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentAnimeBinding
import com.example.proyecto_final_grado.databinding.FragmentDetailsBinding
import com.example.proyecto_final_grado.utils.SessionManager
import com.squareup.picasso.Picasso
import graphql.GetDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.core.text.HtmlCompat
import android.text.Spanned


class AnimeDetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var mediaId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el mediaId del Bundle
        mediaId = arguments?.getInt("MEDIA_ID")

        // Realizar la consulta si el mediaId no es null
        mediaId?.let { id ->
            fetchAnimeDetails(id)
        }
    }

    private fun fetchAnimeDetails(mediaID: Int){
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetDetailQuery(mediaID)).execute()
                val media = response.data?.Media
                Log.d("MediaID", mediaID.toString())

                withContext(Dispatchers.Main){
                    Picasso.get().load(media?.bannerImage).into(binding.bannerImageView)
                    Picasso.get().load(media?.coverImage?.large).into(binding.coverImageView)

                    binding.titleTextView.text = media?.title?.userPreferred
                    val showMoreButton = binding.showMoreButton
                    val cleanedDescription = cleanDescription(media?.description.toString())
                    binding.descriptionTextView.text = cleanedDescription

                    var isExpanded = false

                    showMoreButton.setOnClickListener {
                        if (isExpanded) {
                            binding.descriptionTextView.maxLines = 3
                            binding.descriptionTextView.text = cleanedDescription
                            showMoreButton.setImageResource(R.drawable.ic_arrow_down)
                        } else {
                            binding.descriptionTextView.maxLines = Integer.MAX_VALUE
                            binding.descriptionTextView.text = cleanedDescription
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
                    val dates = "Aired: $startDate to $endDate"
                    binding.dateTextView.text = dates

                    val studios = mutableListOf<String>()
                    val producers = mutableListOf<String>()

                    media?.studios?.edges?.forEach { edge ->
                        val name = edge?.node?.name ?: return@forEach
                        if (edge.isMain) {
                            studios.add(name)
                        } else {
                            producers.add(name)
                        }
                    }
                    val studiosText = "Studios: \n${studios.joinToString(separator = "\n")}"
                    val producersText = "Producers: \n${producers.joinToString(separator = "\n")}"

                    binding.studiosTextView.text = studiosText
                    binding.producersTextView.text = producersText

                    val synonymsList = media?.synonyms?.filterNotNull().orEmpty()
                    binding.synonymsTextView.text = "Synonyms:\n${synonymsList.joinToString("\n")}"

                    val tagsList = media?.tags?.mapNotNull { tag ->
                        val name = tag?.name
                        val rank = tag?.rank
                        if (!name.isNullOrBlank() && rank != null) {
                            "$name - ${rank}%"
                        } else null
                    }.orEmpty()

                    binding.tagsTextView.text = tagsList.joinToString("\n")

                    binding.charactersRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val characters = media?.characters?.nodes?.filterNotNull() ?: emptyList()
                        adapter = CharactersAdapter(characters)
                    }

                    binding.staffRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val staff = media?.staff?.edges?.filterNotNull() ?: emptyList()
                        adapter = StaffAdapter(staff)
                    }

                    binding.relationsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val relations = media?.relations?.edges?.filterNotNull() ?: emptyList()
                        adapter = RelationsAdapter(relations)
                    }


                    //binding.nativeTitleTextView.text = media?.title?.native
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }
        }
    }

    private fun cleanDescription(description: String): Spanned {
            return HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }




}
