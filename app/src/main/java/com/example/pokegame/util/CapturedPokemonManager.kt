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

    // Format: "id|lat|lng" or just "id"
    fun addCaptured(id: Int, lat: Double? = null, lng: Double? = null) {
        val currentSet = getCapturedRawSet().toMutableSet()
        // Remove existing entry for this ID if any (to update location)
        val existing = currentSet.find { it.startsWith("$id|") || it == id.toString() }
        if (existing != null) {
            currentSet.remove(existing)
        }

        val entry = if (lat != null && lng != null) "$id|$lat|$lng" else id.toString()
        currentSet.add(entry)

        prefs.edit().putStringSet(KEY_CAPTURED_IDS, currentSet).apply()
    }

    private fun getCapturedRawSet(): Set<String> {
        return prefs.getStringSet(KEY_CAPTURED_IDS, emptySet()) ?: emptySet()
    }

    fun getCapturedIds(): Set<String> {
        return getCapturedRawSet().map { it.split("|")[0] }.toSet()
    }

    fun getCaptureLocation(id: Int): Pair<Double, Double>? {
        val raw = getCapturedRawSet().find { it.startsWith("$id|") } ?: return null
        val parts = raw.split("|")
        return if (parts.size == 3) {
            Pair(parts[1].toDoubleOrNull() ?: 0.0, parts[2].toDoubleOrNull() ?: 0.0)
        } else {
            null
        }
    }

    fun isCaptured(id: Int): Boolean {
        return getCapturedIds().contains(id.toString())
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
