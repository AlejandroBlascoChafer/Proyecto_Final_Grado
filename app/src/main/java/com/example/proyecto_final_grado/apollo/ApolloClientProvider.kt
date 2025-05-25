package com.example.proyecto_final_grado.apollo

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.apolloStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.network.okHttpClient
import com.example.proyecto_final_grado.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object ApolloClientProvider {

    private var apolloClient: ApolloClient? = null

    fun getApolloClient(context: Context): ApolloClient {
        if (apolloClient == null) {
            val sqlCacheFactory = SqlNormalizedCacheFactory("anilist_cache.db")
            val sessionManager = SessionManager(context)

            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${sessionManager.getAccessToken()}")
                    .build()
                chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            apolloClient = ApolloClient.Builder()
                .serverUrl("https://graphql.anilist.co")
                .normalizedCache(sqlCacheFactory)
                .okHttpClient(okHttpClient)
                .build()
        }

        return apolloClient!!
    }

    fun clearApolloCache(apolloClient: ApolloClient) {
        CoroutineScope(Dispatchers.IO).launch {
            apolloClient.apolloStore.clearAll()
        }
    }
}
