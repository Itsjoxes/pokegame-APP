package com.example.pokegame.util

import com.example.pokegame.R

object TypeIconMapper {
    fun getIconForType(type: String): Int {
        return when (type.lowercase()) {
            "normal" -> R.drawable.ic_type_normal
            // Add other types here when you have the icons
            // "fire" -> R.drawable.ic_type_fire
            // "water" -> R.drawable.ic_type_water
            else -> R.drawable.ic_type_normal // Default icon
        }
    }
}