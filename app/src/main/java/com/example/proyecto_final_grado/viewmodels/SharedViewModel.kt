package com.example.proyecto_final_grado.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.session.SessionManager
import graphql.GetSeasonalAnimeQuery
import graphql.GetTrendingAnimeQuery
import graphql.GetTrendingMangaQuery
import graphql.GetUserAnimeListQuery
import graphql.GetUserMangaListQuery
import graphql.GetUserProfileInfoQuery
import kotlinx.coroutines.launch
import graphql.GetUserProfileInfoQuery.*
import graphql.type.MediaFormat
import graphql.type.MediaSeason
import java.util.Calendar

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val apolloClient = ApolloClientProvider.getApolloClient(application)
    private val sessionManager= SessionManager(application)

    private val _userProfile = MutableLiveData<Viewer?>()
    val userProfile: LiveData<Viewer?> = _userProfile

    private val _animeList = MutableLiveData<List<GetUserAnimeListQuery.Entry?>?>()
    val animeList: LiveData<List<GetUserAnimeListQuery.Entry?>?> = _animeList

    private val _mangaList = MutableLiveData<List<GetUserMangaListQuery.Entry?>?>()
    val mangaList: LiveData<List<GetUserMangaListQuery.Entry?>?> = _mangaList

    private val _likedAnime = MutableLiveData<List<Edge?>?>()
    val likedAnime: LiveData<List<Edge?>?> = _likedAnime

    private val _likedManga = MutableLiveData<List<Edge1?>?>()
    val likedManga: LiveData<List<Edge1?>?> = _likedManga

    private val _likedCharacters = MutableLiveData<List<Edge2?>?>()
    val likedCharacters: LiveData<List<Edge2?>?> = _likedCharacters

    private val _likedStaff = MutableLiveData<List<Edge3?>?>()
    val likedStaff: LiveData<List<Edge3?>?> = _likedStaff

    private val _likedStudio = MutableLiveData<List<Edge4?>?>()
    val likedStudio: LiveData<List<Edge4?>?> = _likedStudio

    private val _trendingAnime = MutableLiveData<List<GetTrendingAnimeQuery.Medium?>?>()
    val trendingAnime: LiveData<List<GetTrendingAnimeQuery.Medium?>?> = _trendingAnime

    private val _trendingManga = MutableLiveData<List<GetTrendingMangaQuery.Medium?>?>()
    val trendingManga: LiveData<List<GetTrendingMangaQuery.Medium?>?> = _trendingManga


    private val _season = MutableLiveData(getCurrentSeason())
    val season: LiveData<MediaSeason> = _season

    private val _year = MutableLiveData(Calendar.getInstance().get(Calendar.YEAR))
    val year: LiveData<Int> = _year

    private val _format = MutableLiveData(MediaFormat.TV)
    val format: LiveData<MediaFormat> = _format

    private val _sortBy = MutableLiveData("Popularity")
    val sortBy: LiveData<String> = _sortBy

    private val _sortOrder = MutableLiveData("Descending")
    val sortOrder: LiveData<String> = _sortOrder

    private val _seasonalAnimeList = MutableLiveData<List<GetSeasonalAnimeQuery.Medium?>>()
    val seasonalAnimeList: LiveData<List<GetSeasonalAnimeQuery.Medium?>> = _seasonalAnimeList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _displayAdultContent = MutableLiveData<Boolean?>()
    val displayAdultContent: MutableLiveData<Boolean?> = _displayAdultContent

    fun loadInitialData() {
        viewModelScope.launch {
            _loading.postValue(true)
            try {

                // Carga datos del perfil con cache primero y luego red
                loadUserProfile()

                // Carga trending anime y manga cache y red
                loadTrendingAnime()
                loadTrendingManga()

                sessionManager.saveUsername(_userProfile.value?.name ?: "")

                // Carga listas de anime y manga igual: cache y luego red
                loadUserAnimeList(sessionManager.getUsername() ?: "")
                loadUserMangaList(sessionManager.getUsername() ?: "")



            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error loading data", e)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private suspend fun loadUserProfile() {
        // Cache
        val cacheResponse = apolloClient.query(GetUserProfileInfoQuery())
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.Viewer?.let {
            _userProfile.postValue(it)
            _likedAnime.postValue(it.favourites?.anime?.edges)
            _likedManga.postValue(it.favourites?.manga?.edges)
            _likedCharacters.postValue(it.favourites?.characters?.edges)
            _likedStaff.postValue(it.favourites?.staff?.edges)
            _likedStudio.postValue(it.favourites?.studios?.edges)
            _displayAdultContent.postValue(it.options?.displayAdultContent)
        }

        // Red
        val networkResponse = apolloClient.query(GetUserProfileInfoQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.Viewer?.let {
            _userProfile.postValue(it)
            _likedAnime.postValue(it.favourites?.anime?.edges)
            _likedManga.postValue(it.favourites?.manga?.edges)
            _likedCharacters.postValue(it.favourites?.characters?.edges)
            _likedStaff.postValue(it.favourites?.staff?.edges)
            _likedStudio.postValue(it.favourites?.studios?.edges)
            _displayAdultContent.postValue(it.options?.displayAdultContent)
        }
    }

    private suspend fun loadUserAnimeList(userName: String) {
        if (userName.isBlank()) return

        val showAdult = displayAdultContent.value == true

        val cacheResponse = apolloClient.query(GetUserAnimeListQuery(userName))
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            val filteredEntries = entries.filter { entry ->
                showAdult || entry?.media?.isAdult == false
            }
            _animeList.postValue(filteredEntries)
        }

        val networkResponse = apolloClient.query(GetUserAnimeListQuery(userName))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            val filteredEntries = entries.filter { entry ->
                showAdult || entry?.media?.isAdult == false
            }
            _animeList.postValue(filteredEntries)
        }
    }

    private suspend fun loadUserMangaList(userName: String) {
        if (userName.isBlank()) return

        val showAdult = displayAdultContent.value == true

        val cacheResponse = apolloClient.query(GetUserMangaListQuery(userName))
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            val filteredEntries = entries.filter { entry ->
                showAdult || entry?.media?.isAdult == false
            }
            _mangaList.postValue(filteredEntries)
        }

        val networkResponse = apolloClient.query(GetUserMangaListQuery(userName))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            val filteredEntries = entries.filter { entry ->
                showAdult || entry?.media?.isAdult == false
            }
            _mangaList.postValue(filteredEntries)
        }
    }


    private suspend fun loadTrendingAnime() {
        val showAdult = displayAdultContent.value == true

        val cacheResponse = apolloClient.query(GetTrendingAnimeQuery())
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.Page?.media?.let {
            val filtered = it.filter { media -> showAdult || media?.isAdult == false }
            _trendingAnime.postValue(filtered)
        }

        val networkResponse = apolloClient.query(GetTrendingAnimeQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.Page?.media?.let {
            val filtered = it.filter { media -> showAdult || media?.isAdult == false }
            _trendingAnime.postValue(filtered)
        }
    }

    private suspend fun loadTrendingManga() {
        val showAdult = displayAdultContent.value == true

        val cacheResponse = apolloClient.query(GetTrendingMangaQuery())
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.Page?.media?.let {
            val filtered = it.filter { media -> showAdult || media?.isAdult == false }
            _trendingManga.postValue(filtered)
        }

        val networkResponse = apolloClient.query(GetTrendingMangaQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.Page?.media?.let {
            val filtered = it.filter { media -> showAdult || media?.isAdult == false }
            _trendingManga.postValue(filtered)
        }
    }


    fun setSeasonalFilters(
        season: MediaSeason = _season.value ?: MediaSeason.WINTER,
        year: Int = _year.value ?: Calendar.getInstance().get(Calendar.YEAR),
        format: MediaFormat = _format.value ?: MediaFormat.TV,
        sortBy: String = _sortBy.value ?: "Popularity",
        sortOrder: String = _sortOrder.value ?: "Descending"
    ) {
        var changed = false
        if (_season.value != season) { _season.value = season; changed = true }
        if (_year.value != year) { _year.value = year; changed = true }
        if (_format.value != format) { _format.value = format; changed = true }
        if (_sortBy.value != sortBy) { _sortBy.value = sortBy; changed = true }
        if (_sortOrder.value != sortOrder) { _sortOrder.value = sortOrder; changed = true }
        if (changed) {
            loadSeasonalAnime()
        }
    }

    fun loadSeasonalAnime() {
        val currentSeason = _season.value ?: MediaSeason.WINTER
        val currentYear = _year.value ?: Calendar.getInstance().get(Calendar.YEAR)
        val currentFormat = _format.value ?: MediaFormat.TV
        val currentSortBy = _sortBy.value ?: "Popularity"
        val currentSortOrder = _sortOrder.value ?: "Descending"
        val showAdult = displayAdultContent.value == true

        viewModelScope.launch {
            try {
                val response = apolloClient.query(
                    GetSeasonalAnimeQuery(currentSeason, currentYear, currentFormat)
                ).fetchPolicy(FetchPolicy.NetworkFirst).execute()

                val mediaList = response.data?.Page?.media
                    ?.filterNotNull()
                    ?.filter { showAdult || it.isAdult == false }
                    ?: emptyList()

                val sortedList = mediaList.sortedWith(compareByDescending {
                    when (currentSortBy) {
                        "Popularity" -> it.popularity ?: 0
                        "Score" -> it.meanScore ?: 0
                        "Favorites" -> it.favourites ?: 0
                        "Trending" -> it.trending ?: 0
                        else -> 0
                    }
                })

                val finalList = if (currentSortOrder == "Ascending") sortedList.reversed() else sortedList

                _seasonalAnimeList.postValue(finalList)

            } catch (e: Exception) {
                _seasonalAnimeList.postValue(emptyList())
            }
        }
    }

    private fun getCurrentSeason(): MediaSeason {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return when (month) {
            in 1..3 -> MediaSeason.WINTER
            in 4..6 -> MediaSeason.SPRING
            in 7..9 -> MediaSeason.SUMMER
            in 10..12 -> MediaSeason.FALL
            else -> MediaSeason.WINTER
        }
    }


}
