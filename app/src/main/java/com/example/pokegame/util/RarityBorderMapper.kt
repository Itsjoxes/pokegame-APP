package com.example.pokegame.util

import com.example.pokegame.R
import com.example.pokegame.api.Pokemon

object RarityBorderMapper {
    fun getBorderColorForRarity(pokemon: Pokemon): Int {
        return when {
            pokemon.isLegendary -> R.color.rarity_legendary
            pokemon.isMythical -> R.color.rarity_epic
            pokemon.baseExperience < 60 -> R.color.rarity_common
            pokemon.baseExperience < 120 -> R.color.rarity_uncommon
            else -> R.color.rarity_rare
        }
    }
}