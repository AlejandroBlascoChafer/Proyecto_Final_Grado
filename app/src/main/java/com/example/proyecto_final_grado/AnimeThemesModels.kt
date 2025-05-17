package com.example.proyecto_final_grado

data class AnimeThemesResponse(
    val anime: List<AnimeThemesAnime>
)

data class AnimeThemesAnime(
    val id: Int,
    val name: String,
    val media_format: String,
    val season: String,
    val slug: String,
    val synopsis: String,
    val year: Int
)

data class AnimeResponse(
    val anime: Anime
)

data class Anime(
    val id: Int,
    val name: String,
    val media_format: String?,
    val season: String?,
    val slug: String,
    val synopsis: String?,
    val year: Int?,
    val animethemes: List<AnimeTheme>?
)

data class AnimeTheme(
    val id: Int,
    val sequence: Int?,         // puede ser null
    val slug: String,
    val type: String,
    val song: Song,
    val animethemeentries: List<AnimeThemeEntry>
)

data class Song(
    val id: Int,
    val title: String,
    val artists: List<Artist>
)

data class Artist(
    val id: Int,
    val name: String,
    val slug: String,
    val information: String?,  // puede ser null
    val artistsong: ArtistSong?
)

data class ArtistSong(
    val alias: String?,   // puede ser null
    val `as`: String?     // nota: "as" es palabra reservada, por eso el backticks
)

data class AnimeThemeEntry(
    val id: Int,
    val episodes: String?,  // puede ser "1-5", "2-", etc.
    val notes: String?,     // puede ser null
    val nsfw: Boolean,
    val spoiler: Boolean,
    val version: String?    // puede ser null
)


