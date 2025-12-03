package com.example.pokegame.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captured_pokemon")
data class CapturedPokemon(
    @PrimaryKey val id: Int,
    val name: String,
    val baseExperience: Int,
    val isLegendary: Boolean,
    val isMythical: Boolean,
    val frontSpriteUrl: String?,
    val types: List<String> // Store the list of type names
)
