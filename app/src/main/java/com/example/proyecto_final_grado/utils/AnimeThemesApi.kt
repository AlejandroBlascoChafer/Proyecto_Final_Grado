package com.example.proyecto_final_grado.utils

import AnimeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeThemesApi {

    // Buscar anime por nombre (slug) - devuelve lista
    @GET("anime")
    suspend fun getAnimeByName(
        @Query("filter[name-like]") name: String
    ): AnimeResponse

    // Obtener info de un anime por slug, puede devolver un objeto o lista, usando AnimeResponse con deserializador
    @GET("anime/{slug}")
    suspend fun getAnimeDetails(
        @Path("slug") slug: String,
        @Query("include") include: String = "animethemes.song.artists,animethemes.animethemeentries"
    ): AnimeResponse
}
