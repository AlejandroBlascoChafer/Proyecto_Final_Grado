package com.example.proyecto_final_grado.session


import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val KEY_USERNAME = "username"
    }

    fun saveAccessToken(token: String) {
        prefs.edit().putString(ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }

    fun clearSession() {
        prefs.edit().remove(ACCESS_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

}
