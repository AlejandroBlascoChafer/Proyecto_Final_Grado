data class AnimeResponse(
    val animeList: List<Anime>
)

data class Anime(
    val id: Int,
    val name: String,
    val media_format: String?,
    val season: String?,
    val slug: String,
    val synopsis: String?,
    val year: Int?,
    val animethemes: List<Animetheme>? = null
)

data class Animetheme(
    val id: Int,
    val sequence: Int,
    val slug: String,
    val type: String,
    val song: Song,
    val animethemeentries: List<AnimethemeEntry>
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
    val information: String?,
    val artistsong: ArtistSong?
)

data class ArtistSong(
    val alias: String?,
    val `as`: String?
)

data class AnimethemeEntry(
    val id: Int,
    val episodes: String,
    val notes: String?,
    val nsfw: Boolean,
    val spoiler: Boolean,
    val version: Int?
)

data class ThemeInfo(
    val type: String,
    val title: String,
    val artists: List<String>,
    val episodes: String,
    val slug: String
)

