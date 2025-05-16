package com.example.proyecto_final_grado.utils

import Anime
import AnimeResponse
import com.google.gson.*
import java.lang.reflect.Type

class AnimeResponseDeserializer : JsonDeserializer<AnimeResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AnimeResponse {
        val jsonObject = json.asJsonObject
        val animeElement = jsonObject.get("anime")

        val animeList = mutableListOf<Anime>()

        if (animeElement.isJsonArray) {
            val jsonArray = animeElement.asJsonArray
            jsonArray.forEach { element ->
                val anime = context?.deserialize<Anime>(element, Anime::class.java)
                if (anime != null) animeList.add(anime)
            }
        } else if (animeElement.isJsonObject) {
            val anime = context?.deserialize<Anime>(animeElement.asJsonObject, Anime::class.java)
            if (anime != null) animeList.add(anime)
        }

        return AnimeResponse(animeList)
    }
}
