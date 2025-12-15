package com.example.pokegame.api

import com.google.gson.annotations.SerializedName

data class PokemonSpecies(
        val id: Int,
        val name: String,
        @SerializedName("is_legendary") val isLegendary: Boolean,
        @SerializedName("is_mythical") val isMythical: Boolean,
        @SerializedName("flavor_text_entries")
        val flavorTextEntries: List<FlavorTextEntry> = emptyList()
)
