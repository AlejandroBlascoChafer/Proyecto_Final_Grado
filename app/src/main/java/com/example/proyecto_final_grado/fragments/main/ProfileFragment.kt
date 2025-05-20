package com.example.proyecto_final_grado.fragments.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.login.LoginActivity
import com.example.proyecto_final_grado.activities.MainActivity
import com.example.proyecto_final_grado.adapters.details.StudioAdapter
import com.example.proyecto_final_grado.databinding.FragmentProfileBinding
import com.example.proyecto_final_grado.adapters.profile.FavouritesAdapter
import com.example.proyecto_final_grado.fragments.details.AnimeDetailsFragment
import com.example.proyecto_final_grado.fragments.details.CharacterDetailsFragment
import com.example.proyecto_final_grado.fragments.details.MangaDetailsFragment
import com.example.proyecto_final_grado.fragments.details.StaffDetailsFragment
import com.example.proyecto_final_grado.fragments.details.StudiosDetailsFragment
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.listeners.OnCharacterClickListener
import com.example.proyecto_final_grado.listeners.OnMangaClickListener
import com.example.proyecto_final_grado.listeners.OnStaffClickListener
import com.example.proyecto_final_grado.listeners.OnStudioClickListener
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_ANIME
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_CHARACTER
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_MANGA
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_STAFF
import com.example.proyecto_final_grado.utils.Constants.FAV_TYPE_STUDIO
import com.example.proyecto_final_grado.utils.SessionManager
import com.example.proyecto_final_grado.utils.SharedViewModel
import com.example.proyecto_final_grado.utils.openMediaDetailFragment
import com.squareup.picasso.Picasso
import java.util.Locale

class ProfileFragment : Fragment(), OnCharacterClickListener, OnStaffClickListener, OnAnimeClickListener, OnMangaClickListener, OnStudioClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager


    private val sharedViewModel: SharedViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())



        binding.settingsButton.setOnClickListener {
            showPopupMenu(it)
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.userProfile.observe(viewLifecycleOwner) { viewer ->
            if (viewer != null) {
                binding.usernameText.text = viewer.name
                viewer.avatar?.large?.let { url ->
                    Picasso.get().load(url).into(binding.profileImage)
                }
                binding.bioText.text = viewer.about

                // Estadísticas de anime
                binding.statTotalAnime.text = viewer.statistics?.anime?.count?.toString() ?: "0"
                binding.statEpisodes.text = viewer.statistics?.anime?.episodesWatched?.toString() ?: "0"
                val daysWatched = viewer.statistics?.anime?.minutesWatched?.toDouble()?.div(60 * 24) ?: 0.0
                binding.statDays.text = String.format(Locale.getDefault(), "%.1f", daysWatched)
                binding.statAnimeScore.text = viewer.statistics?.anime?.meanScore?.toString() ?: "-"

                // Estadísticas de manga
                binding.statTotalManga.text = viewer.statistics?.manga?.count?.toString() ?: "0"
                binding.statChapters.text = viewer.statistics?.manga?.chaptersRead?.toString() ?: "0"
                binding.statVolumes.text = viewer.statistics?.manga?.volumesRead?.toString() ?: "0"
                binding.statMangaScore.text = viewer.statistics?.manga?.meanScore?.toString() ?: "-"
            }
        }
        sharedViewModel.likedAnime.observe(viewLifecycleOwner) { updateRecyclerViews() }
        sharedViewModel.likedManga.observe(viewLifecycleOwner) { updateRecyclerViews() }
        sharedViewModel.likedCharacters.observe(viewLifecycleOwner) { updateRecyclerViews() }
        sharedViewModel.likedStaff.observe(viewLifecycleOwner) { updateRecyclerViews() }
        sharedViewModel.likedStudio.observe(viewLifecycleOwner) { updateRecyclerViews() }
    }
    private fun updateRecyclerViews() {
        val anime = sharedViewModel.likedAnime.value?.filterNotNull() ?: emptyList()
        val manga = sharedViewModel.likedManga.value?.filterNotNull() ?: emptyList()
        val characters = sharedViewModel.likedCharacters.value?.filterNotNull() ?: emptyList()
        val staff = sharedViewModel.likedStaff.value?.filterNotNull() ?: emptyList()
        val studio = sharedViewModel.likedStudio.value?.filterNotNull() ?: emptyList()

        setupRecyclerView(anime, manga, characters, staff, studio)
    }

    private fun setupRecyclerView(animeList: List<Any>,
                                  mangaList: List<Any>,
                                  charactersList: List<Any>,
                                  staffList: List<Any>,
                                  studioList: List<Any>) {
        binding.favAnimeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FavouritesAdapter(animeList, FAV_TYPE_ANIME, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment)
        }

        binding.favMangaRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FavouritesAdapter(mangaList, FAV_TYPE_MANGA, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment)
        }

        binding.favCharactersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FavouritesAdapter(charactersList, FAV_TYPE_CHARACTER, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment)
        }

        binding.favStaffRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FavouritesAdapter(staffList, FAV_TYPE_STAFF, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment)
        }

        binding.favStudioRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FavouritesAdapter(studioList, FAV_TYPE_STUDIO, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment, this@ProfileFragment)
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

        if (studioList.isEmpty()){
            binding.tvStudio.visibility = View.GONE
            binding.favStudioRecyclerView.visibility = View.GONE
        } else {
            binding.tvStudio.visibility = View.VISIBLE
            binding.favStudioRecyclerView.visibility = View.VISIBLE
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_profile_popup, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    sessionManager.clearSession()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
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

    override fun onStudioClick(studioName: String) {
        val studiofragment = StudiosDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("NAME", studioName)
            }
        }
        (activity as? MainActivity)?.openDetailFragment(studiofragment)
    }
}
