package com.example.proyecto_final_grado.models

sealed class SearchItem {
    data class AnimeMangaItem(
        val id: Int,
        val title: String,
        val imageUrl: String,
        val format: String?,
        val favourites: Int?,
        val meanScore: Int?,
        val type: String?
    ) : SearchItem()

    data class CharacterItem(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val favourites: Int?
    ) : SearchItem()

    data class StaffItem(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val favourites: Int?
    ) : SearchItem()

    data class StudioItem(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val favourites: Int?
    ) : SearchItem()
}