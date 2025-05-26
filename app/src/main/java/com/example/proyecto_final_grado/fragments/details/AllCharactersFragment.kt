package com.example.proyecto_final_grado.fragments.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.adapters.details.AllCharactersAdapter
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentAllCharactersBinding
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.ui.openMediaDetailFragment
import graphql.GetMediaDetailQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllCharactersFragment : Fragment(), OnCharacterClickListener, OnStaffClickListener {

    private var _binding: FragmentAllCharactersBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient
    private var mediaID: Int? = null
    private var allCharacters: List<GetMediaDetailQuery.Edge?> = emptyList()

    private var languageChangeRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var currentLanguage = "Japanese"

    private lateinit var charactersAdapter: AllCharactersAdapter

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selected_language", currentLanguage)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaID = arguments?.getInt("MEDIA_ID")

        if (savedInstanceState != null) {
            currentLanguage = savedInstanceState.getString("selected_language", "Japanese")
        }

        setupLanguageSelector(currentLanguage)
        mediaID?.let { fetchCharacters(it) }
    }

    private fun setupLanguageSelector(selectedLanguage: String) {
        val languages = listOf(
            "Japanese", "English", "Korean", "Italian", "Spanish", "Portuguese", "French",
            "German", "Hebrew", "Hungarian", "Chinese", "Arabic", "Filipino", "Catalan",
            "Finnish", "Turkish", "Dutch", "Swedish", "Thai", "Tagalog", "Malaysian",
            "Indonesian", "Vietnamese", "Nepali", "Hindi", "Urdu"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        binding.languageSelector.setAdapter(adapter)
        binding.languageSelector.setText(selectedLanguage, false)

        binding.languageSelector.setOnItemClickListener { _, _, position, _ ->
            val language = languages[position]
            if (language != currentLanguage) {
                currentLanguage = language
                binding.allCharactersRecyclerView.visibility = View.INVISIBLE
                languageChangeRunnable?.let { handler.removeCallbacks(it) }
                languageChangeRunnable = Runnable {
                    (binding.allCharactersRecyclerView.adapter as? AllCharactersAdapter)?.setVoiceActorLanguage(language)
                    binding.allCharactersRecyclerView.visibility = View.VISIBLE
                }
                handler.postDelayed(languageChangeRunnable!!, 300)
            }
        }

        // Inicializa el adapter con el idioma actual
        (binding.allCharactersRecyclerView.adapter as? AllCharactersAdapter)?.setVoiceActorLanguage(currentLanguage)
    }


    override fun onResume() {
        super.onResume()
        // Cada vez que el fragmento está visible, resetea el adapter con todos los idiomas y el texto actual
        setupLanguageSelector(currentLanguage)
    }


    private fun fetchCharacters(mediaID: Int) {
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient.query(GetMediaDetailQuery(mediaID)).execute()

                withContext(Dispatchers.Main) {
                    allCharacters = response.data?.Media?.characters?.edges ?: emptyList()
                    charactersAdapter = AllCharactersAdapter(allCharacters, currentLanguage, this@AllCharactersFragment, this@AllCharactersFragment)
                    binding.allCharactersRecyclerView.apply {
                        layoutManager = GridLayoutManager(requireContext(), 2)
                        adapter = charactersAdapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStaffClick(mediaID: Int) {
        openMediaDetailFragment(mediaID) { StaffDetailsFragment() }
    }
}
