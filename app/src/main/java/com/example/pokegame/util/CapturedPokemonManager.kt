package com.example.pokegame.util

import android.content.Context
import android.content.SharedPreferences

object CapturedPokemonManager {
    private const val PREF_NAME = "captured_pokemon_prefs"
    private const val KEY_CAPTURED_IDS = "captured_ids"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun addCaptured(id: Int) {
        val currentIds = getCapturedIds().toMutableSet()
        currentIds.add(id.toString())
        prefs.edit().putStringSet(KEY_CAPTURED_IDS, currentIds).apply()
    }

    fun getCapturedIds(): Set<String> {
        return prefs.getStringSet(KEY_CAPTURED_IDS, emptySet()) ?: emptySet()
    }

    fun isCaptured(id: Int): Boolean {
        return getCapturedIds().contains(id.toString())
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
