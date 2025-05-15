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

    private val _likedAnime = MutableLiveData<List<Edge?>?>()
    val likedAnime: LiveData<List<Edge?>?> = _likedAnime

    private val _likedManga = MutableLiveData<List<Edge1?>?>()
    val likedManga: LiveData<List<Edge1?>?> = _likedManga

    private val _likedCharacters = MutableLiveData<List<Edge2?>?>()
    val likedCharacters: LiveData<List<Edge2?>?> = _likedCharacters

    private val _likedStaff = MutableLiveData<List<Edge3?>?>()
    val likedStaff: LiveData<List<Edge3?>?> = _likedStaff


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadInitialData() {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val profileResponse = apolloClient.query(GetUserProfileInfoQuery()).execute()
                val userName = profileResponse.data?.Viewer?.name.toString()
                val animeResponse = apolloClient.query(GetUserAnimeListQuery(userName)).execute()
                val mangaResponse = apolloClient.query(GetUserMangaListQuery(userName)).execute()

                _userProfile.value = profileResponse.data?.Viewer
                _animeList.value = animeResponse.data?.MediaListCollection?.lists?.flatMap { it?.entries ?: emptyList() }
                _mangaList.value = mangaResponse.data?.MediaListCollection?.lists?.flatMap { it?.entries ?: emptyList()  }
                _likedAnime.value = profileResponse.data?.Viewer?.favourites?.anime?.edges
                _likedManga.value = profileResponse.data?.Viewer?.favourites?.manga?.edges
                _likedCharacters.value = profileResponse.data?.Viewer?.favourites?.characters?.edges
                _likedStaff.value = profileResponse.data?.Viewer?.favourites?.staff?.edges


            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error loading data", e)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
