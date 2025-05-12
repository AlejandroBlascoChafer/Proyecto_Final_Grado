package com.example.proyecto_final_grado.utils

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import graphql.GetUserAnimeListQuery
import graphql.GetUserMangaListQuery
import graphql.GetUserProfileInfoQuery
import kotlinx.coroutines.launch
import graphql.GetUserProfileInfoQuery.*
import graphql.type.MediaListStatus

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val apolloClient = ApolloClientProvider.getApolloClient(application)

    private val _userProfile = MutableLiveData<Viewer?>()
    val userProfile: LiveData<Viewer?> = _userProfile

    private val _animeList = MutableLiveData<List<GetUserAnimeListQuery.Entry?>?>()
    val animeList: LiveData<List<GetUserAnimeListQuery.Entry?>?> = _animeList

    private val _mangaList = MutableLiveData<List<GetUserMangaListQuery.Entry?>?>()
    val mangaList: LiveData<List<GetUserMangaListQuery.Entry?>?> = _mangaList

    private val _likedAnime = MutableLiveData<List<Node?>?>()
    val likedAnime: LiveData<List<Node?>?> = _likedAnime

    private val _likedManga = MutableLiveData<List<Node1?>?>()
    val likedManga: LiveData<List<Node1?>?> = _likedManga

    private val _likedCharacters = MutableLiveData<List<Node2?>?>()
    val likedCharacters: LiveData<List<Node2?>?> = _likedCharacters

    private val _likedStaff = MutableLiveData<List<Node3?>?>()
    val likedStaff: LiveData<List<Node3?>?> = _likedStaff


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadInitialData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val profileResponse = apolloClient.query(GetUserProfileInfoQuery()).execute()
                val userName = profileResponse.data?.Viewer?.name.toString()
                val animeResponse = apolloClient.query(GetUserAnimeListQuery(userName, MediaListStatus.valueOf("CURRENT"))).execute()
                val mangaResponse = apolloClient.query(GetUserMangaListQuery(userName, MediaListStatus.valueOf("CURRENT"))).execute()

                _userProfile.value = profileResponse.data?.Viewer
                _animeList.value = animeResponse.data?.MediaListCollection?.lists?.get(0)?.entries
                _mangaList.value = mangaResponse.data?.MediaListCollection?.lists?.get(0)?.entries
                _likedAnime.value = profileResponse.data?.Viewer?.favourites?.anime?.nodes
                _likedManga.value = profileResponse.data?.Viewer?.favourites?.manga?.nodes
                _likedCharacters.value = profileResponse.data?.Viewer?.favourites?.characters?.nodes
                _likedStaff.value = profileResponse.data?.Viewer?.favourites?.staff?.nodes
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error loading data", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
