package com.example.proyecto_final_grado.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.Proyecto_Final_Grado.queries.GetUserProfileInfoQuery.*
import com.Proyecto_Final_Grado.queries.GetUserProfileInfoQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.example.proyecto_final_grado.adapters.FAV_TYPE_ANIME
import com.example.proyecto_final_grado.adapters.FAV_TYPE_CHARACTER
import com.example.proyecto_final_grado.adapters.FAV_TYPE_MANGA
import com.example.proyecto_final_grado.adapters.FAV_TYPE_STAFF
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.FragmentProfileBinding
import com.example.proyecto_final_grado.adapters.LikesAdapter
import com.example.proyecto_final_grado.utils.SessionManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private lateinit var apolloClient: ApolloClient





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())


        fetchUserData()
        return binding.root
    }

    private fun setupRecyclerView(animeList: List<Any>,
                                  mangaList: List<Any>,
                                  charactersList: List<Any>,
                                  staffList: List<Any>) {
        binding.favAnimeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = LikesAdapter(animeList, FAV_TYPE_ANIME)
        }

        binding.favMangaRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = LikesAdapter(mangaList, FAV_TYPE_MANGA)
        }

        binding.favCharactersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = LikesAdapter(charactersList, FAV_TYPE_CHARACTER)
        }

        binding.favStaffRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = LikesAdapter(staffList, FAV_TYPE_STAFF)
        }
        if (animeList.isEmpty()) {
            binding.favAnimeRecyclerView.visibility = View.GONE
            binding.tvAnime.visibility = View.GONE
        } else {
            binding.favAnimeRecyclerView.visibility = View.VISIBLE
            binding.tvAnime.visibility = View.VISIBLE
        }

        if (mangaList.isEmpty()) {
            binding.favMangaRecyclerView.visibility = View.GONE
            binding.tvManga.visibility = View.GONE
        } else {
            binding.favMangaRecyclerView.visibility = View.VISIBLE
            binding.tvManga.visibility = View.VISIBLE
        }

        if (charactersList.isEmpty()) {
            binding.favCharactersRecyclerView.visibility = View.GONE
            binding.tvCharacters.visibility = View.GONE
        } else {
            binding.favCharactersRecyclerView.visibility = View.VISIBLE
            binding.tvCharacters.visibility = View.VISIBLE
        }

        if (staffList.isEmpty()) {
            binding.favStaffRecyclerView.visibility = View.GONE
            binding.tvStaff.visibility = View.GONE
        } else {
            binding.favStaffRecyclerView.visibility = View.VISIBLE
            binding.tvStaff.visibility = View.VISIBLE
        }
    }

    private fun fetchUserData() {

        apolloClient = ApolloClientProvider.getApolloClient(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ApolloResponse<Data> =
                    apolloClient.query(GetUserProfileInfoQuery()).execute()

                val viewer = response.data?.Viewer

                val animeList = viewer?.favourites?.anime?.nodes?.filterNotNull() ?: emptyList()
                val mangaList = viewer?.favourites?.manga?.nodes?.filterNotNull() ?: emptyList()
                val charactersList = viewer?.favourites?.characters?.nodes?.filterNotNull() ?: emptyList()
                val staffList = viewer?.favourites?.staff?.nodes?.filterNotNull() ?: emptyList()









                withContext(Dispatchers.Main) {
                    if (viewer != null) {
                        binding.usernameText.text = viewer.name
                    }
                    if (viewer != null) {
                        viewer.avatar?.large?.let { url ->
                            Picasso.get().load(url).into(binding.profileImage)
                        }
                    }

                }
                setupRecyclerView(animeList, mangaList, charactersList, staffList)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
