package com.example.proyecto_final_grado.utils

import AnimeResponse
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = "https://api.animethemes.moe/"

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    // Aquí creamos Gson con el deserializador personalizado
    private val gson = GsonBuilder()
        .registerTypeAdapter(AnimeResponse::class.java, AnimeResponseDeserializer())
        .create()

    val api: AnimeThemesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)) // <-- Usamos el gson con deserializador
            .build()
            .create(AnimeThemesApi::class.java)
    }
}
