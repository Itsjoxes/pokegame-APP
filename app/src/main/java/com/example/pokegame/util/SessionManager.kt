package com.example.pokegame.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_TOKEN = "auth_token"
    }

    fun saveSession(username: String, password: String? = null) {
        val editor = prefs.edit()
        editor.putString(KEY_USERNAME, username)
        if (password != null) {
            editor.putString(KEY_PASSWORD, password)
        }
        editor.apply()
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getPassword(): String? {
        return prefs.getString(KEY_PASSWORD, null)
    }

    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
