package com.example.proyecto_final_grado.utils

import com.example.proyecto_final_grado.AnimeThemesAnime
import com.example.proyecto_final_grado.AnimeThemesResponse
import com.google.gson.*
import java.lang.reflect.Type

class AnimeResponseDeserializer : JsonDeserializer<AnimeThemesResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AnimeThemesResponse {
        val jsonObject = json.asJsonObject
        val animeElement = jsonObject.get("anime")

        val animeList = mutableListOf<AnimeThemesAnime>()

        if (animeElement.isJsonArray) {
            val jsonArray = animeElement.asJsonArray
            jsonArray.forEach { element ->
                val anime = context?.deserialize<AnimeThemesAnime>(element, AnimeThemesAnime::class.java)
                if (anime != null) animeList.add(anime)
            }
        } else if (animeElement.isJsonObject) {
            val anime = context?.deserialize<AnimeThemesAnime>(animeElement.asJsonObject, AnimeThemesAnime::class.java)
            if (anime != null) animeList.add(anime)
        }

        return AnimeThemesResponse(animeList)
    }
}
