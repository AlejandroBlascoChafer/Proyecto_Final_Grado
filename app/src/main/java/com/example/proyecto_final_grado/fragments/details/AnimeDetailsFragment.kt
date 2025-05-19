package com.example.proyecto_final_grado.fragments.details

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.LineHeightSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.details.CharactersMediaAdapter
import com.example.proyecto_final_grado.adapters.details.ExternalLinksAdapter
import com.example.proyecto_final_grado.adapters.details.RecommendationsAdapter
import com.example.proyecto_final_grado.adapters.details.RelationsAdapter
import com.example.proyecto_final_grado.adapters.details.StaffAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentDetailsBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import graphql.GetMediaDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class AnimeDetailsFragment : Fragment(), OnCharacterClickListener, OnAnimeClickListener, OnMangaClickListener, OnStaffClickListener {

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
                val response = apolloClient.query(GetMediaDetailQuery(mediaID)).execute()
                val media = response.data?.Media
                Log.d("MediaID", mediaID.toString())

                withContext(Dispatchers.Main){
                    Picasso.get().load(media?.bannerImage).into(binding.bannerImageView)
                    Picasso.get().load(media?.coverImage?.large).into(binding.coverImageView)


                    binding.titleTextView.text = media?.title?.userPreferred
                    binding.nativeTitleTextView.text = media?.title?.native

                    val showMoreButton = binding.showMoreButton
                    markwon.setMarkdownText(requireContext(), binding.descriptionTextView, media?.description)
                    var isDescriptionExpanded = false

                    showMoreButton.setOnClickListener {
                        if (isDescriptionExpanded) {
                            binding.descriptionTextView.maxLines = 3
                            showMoreButton.setImageResource(R.drawable.ic_arrow_down)
                        } else {
                            binding.descriptionTextView.maxLines = Integer.MAX_VALUE
                            showMoreButton.setImageResource(R.drawable.ic_arrow_up)
                        }

                        isDescriptionExpanded = !isDescriptionExpanded
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
                    binding.studiosTextView.apply {
                        text = buildClickableList(studios, "Studio") { name ->

                        }
                        movementMethod = LinkMovementMethod.getInstance()
                        highlightColor = Color.TRANSPARENT
                    }

                    binding.producersTextView.apply {
                        text = buildClickableList(producers, "Producer") { name ->

                        }
                        movementMethod = LinkMovementMethod.getInstance()
                        highlightColor = Color.TRANSPARENT
                    }

                    val synonymsList = media?.synonyms?.filterNotNull().orEmpty()
                    binding.synonymsTextView.text = "Synonyms:\n${synonymsList.joinToString("\n")}"

                    val tagsList = media?.tags?.mapNotNull { tag ->
                        val name = tag?.name
                        val rank = tag?.rank
                        if (!name.isNullOrBlank() && rank != null) {
                            "$name - ${rank}%"
                        } else null
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



                    val mainCharacters = media?.characters?.edges?.filter { it?.role?.name == "MAIN" }

                    binding.charactersRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val characters = mainCharacters?.filterNotNull() ?: emptyList()
                        adapter = CharactersMediaAdapter(characters, this@AnimeDetailsFragment)
                    }

                    binding.staffRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val staff = media?.staff?.edges?.filterNotNull() ?: emptyList()
                        adapter = StaffAdapter(staff, this@AnimeDetailsFragment)
                    }

                    binding.relationsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val relations = media?.relations?.edges?.filterNotNull() ?: emptyList()
                        adapter = RelationsAdapter(relations, this@AnimeDetailsFragment, this@AnimeDetailsFragment)
                    }

                    binding.recommendationsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val recommendations = media?.recommendations?.edges?.filterNotNull() ?: emptyList()
                        adapter = RecommendationsAdapter(recommendations, this@AnimeDetailsFragment, this@AnimeDetailsFragment)
                    }

                    binding.externalLinksRecyclerView.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        val links = media?.externalLinks?.filterNotNull() ?: emptyList()
                        adapter = ExternalLinksAdapter(links, this@AnimeDetailsFragment, this@AnimeDetailsFragment)
                    }

                    binding.showMoreCharacters.setOnClickListener{
                        val allCharactersFragment = AllCharactersFragment().apply {
                            // Pasar el ID del anime al fragmento de detalle usando un Bundle
                            arguments = Bundle().apply {
                                putInt("MEDIA_ID", mediaID)
                            }
                        }

                        // Iniciar la transacción del fragmento
                        (activity as? MainActivity)?.openDetailFragment(allCharactersFragment)
                    }


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }
        }
    }

    private fun buildClickableList(names: List<String>, type:String, onClick: (String) -> Unit): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()
        if (type == "Studio") {
            spannable.append("Studios:\n")
        } else if (type == "Producer"){
            spannable.append("Producers:\n")
        }



        for ((index, name) in names.withIndex()) {
            val start = spannable.length
            spannable.append(name)
            val end = spannable.length

            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick(name)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(requireContext(), R.color.accentColor) // Azul
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannable.setSpan(object : LineHeightSpan {
                override fun chooseHeight(
                    text: CharSequence,
                    start: Int,
                    end: Int,
                    spanstartv: Int,
                    v: Int,
                    fm: Paint.FontMetricsInt
                ) {
                    val extraSpace = 20 // píxeles extra entre líneas
                    fm.descent += extraSpace
                    fm.bottom += extraSpace
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (index < names.size - 1) {
                spannable.append("\n") // o ", "
            }
        }

        return spannable
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
