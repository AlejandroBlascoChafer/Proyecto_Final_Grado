package com.example.proyecto_final_grado.utils

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import graphql.GetTrendingAnimeQuery
import graphql.GetTrendingMangaQuery
import graphql.GetUserAnimeListQuery
import graphql.GetUserMangaListQuery
import graphql.GetUserProfileInfoQuery
import kotlinx.coroutines.launch
import graphql.GetUserProfileInfoQuery.*

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val apolloClient = ApolloClientProvider.getApolloClient(application)

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

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadInitialData() {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                // Carga datos del perfil con cache primero y luego red
                loadUserProfile()

                val userName = _userProfile.value?.name ?: ""

                // Carga listas de anime y manga igual: cache y luego red
                loadUserAnimeList(userName)
                loadUserMangaList(userName)

                // Carga trending anime y manga cache y red
                loadTrendingAnime()
                loadTrendingManga()

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
        }
    }

    private suspend fun loadUserAnimeList(userName: String) {
        if (userName.isBlank()) return

        val cacheResponse = apolloClient.query(GetUserAnimeListQuery(userName))
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            _animeList.postValue(entries)
        }

        val networkResponse = apolloClient.query(GetUserAnimeListQuery(userName))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            _animeList.postValue(entries)
        }
    }

    private suspend fun loadUserMangaList(userName: String) {
        if (userName.isBlank()) return

        val cacheResponse = apolloClient.query(GetUserMangaListQuery(userName))
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            _mangaList.postValue(entries)
        }

        val networkResponse = apolloClient.query(GetUserMangaListQuery(userName))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.MediaListCollection?.lists?.let {
            val entries = it.flatMap { list -> list?.entries ?: emptyList() }
            _mangaList.postValue(entries)
        }
    }

    private suspend fun loadTrendingAnime() {
        val cacheResponse = apolloClient.query(GetTrendingAnimeQuery())
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.Page?.media?.let {
            _trendingAnime.postValue(it)
        }

        val networkResponse = apolloClient.query(GetTrendingAnimeQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.Page?.media?.let {
            _trendingAnime.postValue(it)
        }
    }

    private suspend fun loadTrendingManga() {
        val cacheResponse = apolloClient.query(GetTrendingMangaQuery())
            .fetchPolicy(FetchPolicy.CacheOnly)
            .execute()

        cacheResponse.data?.Page?.media?.let {
            _trendingManga.postValue(it)
        }

        val networkResponse = apolloClient.query(GetTrendingMangaQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

        networkResponse.data?.Page?.media?.let {
            _trendingManga.postValue(it)
        }
    }
}
