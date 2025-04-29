package com.example.proyecto_final_grado.models

data class Anime(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val score: Double?,
    val status: String
)