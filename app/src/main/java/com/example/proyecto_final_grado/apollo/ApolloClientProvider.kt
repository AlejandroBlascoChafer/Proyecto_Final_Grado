package com.example.proyecto_final_grado.apollo

import com.apollographql.apollo.ApolloClient

object ApolloClientProvider {
    private const val BASE_URL = "https://api.anilist.co/graphql"

    val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl(BASE_URL)
            .build()
    }
}