package com.example.proyecto_final_grado.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.AnimeTheme
import com.example.proyecto_final_grado.AnimeThemesAnime
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.CharactersMediaAdapter
import com.example.proyecto_final_grado.adapters.RelationsAdapter
import com.example.proyecto_final_grado.adapters.StaffAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentDetailsBinding
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.utils.AnimeThemesApi
import com.example.proyecto_final_grado.utils.MarkdownUtils
import com.example.proyecto_final_grado.utils.RetrofitClient
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
    private lateinit var api: AnimeThemesApi

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
        api = RetrofitClient.api
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetMediaDetailQuery(mediaID)).execute()
                val media = response.data?.Media
                Log.d("MediaID", mediaID.toString())

                withContext(Dispatchers.Main){
                    Picasso.get().load(media?.bannerImage).into(binding.bannerImageView)
                    Picasso.get().load(media?.coverImage?.large).into(binding.coverImageView)

                    val title = media?.title?.userPreferred
                    binding.titleTextView.text = title

                    if (title != null) {
                        // Lanzar corrutina para obtener el slug
                        CoroutineScope(Dispatchers.IO).launch {
                            val slug = fetchAnimeSlug(title)
                            if (slug != null) {
                                Log.d("AnimeSlug", "Slug encontrado: $slug")
                                // Aquí haces la llamada siguiente, por ejemplo:
                                val animeDetails = fetchSongsAndArtists(slug)
                                withContext(Dispatchers.Main) {
                                    showAnimeThemes(animeDetails, binding.openingsTextView, binding.endingsTextView)

                                }
                                // o actualizar UI con esos datos
                            } else {
                                Log.d("AnimeSlug", "Slug no encontrado para $title")
                            }
                        }
                    }

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
                    val suppCharacters = media?.characters?.edges?.filter { it?.role?.name == "SUPPORTING" }
                    val backCharacters = media?.characters?.edges?.filter { it?.role?.name == "BACKGROUND" }

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


                    //binding.nativeTitleTextView.text = media?.title?.native
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }
        }
    }

    private fun cleanTitle(title: String): String {
        // Elimina "Part 1", "Part 2", "Part I", etc., ignorando mayúsculas y espacios extras
        val regex = Regex("""\bpart\s*[0-9ivx]+\b""", RegexOption.IGNORE_CASE)
        return title.replace(regex, "")
            .replace("  ", " ")  // Quita dobles espacios
            .trim()
    }

    private fun titlesMatch(title1: String, title2: String): Boolean {
        val clean1 = cleanTitle(title1).lowercase()
        val clean2 = cleanTitle(title2).lowercase()
        // Aquí comparo igualdad exacta (limpia y en minúsculas)
        if (clean1 == clean2) return true

        // Puedes añadir más heurísticas si quieres, por ejemplo:
        // - Ignorar diferencias de tildes, acentos (usa Normalizer)
        // - Permitir pequeñas diferencias con distancia Levenshtein (requiere librería)

        return false
    }

    suspend fun fetchAnimeThemesMatches(
        searchTitle: String,
        api: AnimeThemesApi
    ): List<AnimeThemesAnime> {
        val cleanedSearchTitle = cleanTitle(searchTitle).lowercase()

        val response = api.getAnimeByName(searchTitle)
        val candidates = response.anime
        Log.d("Respuesta AnimeThemes", candidates.toString())

        return candidates.filter { candidate ->
            titlesMatch(candidate.name, cleanedSearchTitle)
        }
    }




    private suspend fun fetchAnimeSlug(animeName: String): String? {
        return try {
            val response = RetrofitClient.api.getAnimeByName(animeName)
            Log.d("Data", "$response")
            val data = response.anime
            Log.d("Data", "$data")
            if (data.isNotEmpty()) {
                data[0].slug
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
//
//
    private suspend fun fetchSongsAndArtists(slug: String): List<AnimeTheme>? {
        return try {
            val response = RetrofitClient.api.getAnimeDetails(slug)
            response.anime.animethemes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showAnimeThemes(animethemes: List<AnimeTheme>?, tvOpenings: TextView, tvEndings: TextView) {
        if (animethemes == null) {
            tvOpenings.text = "No openings found."
            tvEndings.text = "No endings found."
            return
        }

        val openingsText = StringBuilder()
        val endingsText = StringBuilder()

        animethemes.forEach { theme ->
            val artists = theme.song.artists.joinToString(", ") { it.name }
            val episodes = theme.animethemeentries.joinToString(", ") { it.episodes ?: "N/A" }
            val infoLine = "Song: ${theme.song.title}\nArtists: $artists\nEpisodes: $episodes\n\n"

            when (theme.type) {
                "OP" -> openingsText.append(infoLine)
                "ED" -> endingsText.append(infoLine)
            }
        }

        tvOpenings.text = if (openingsText.isNotEmpty()) openingsText.toString() else "No openings found."
        tvEndings.text = if (endingsText.isNotEmpty()) endingsText.toString() else "No endings found."
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
