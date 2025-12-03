package com.example.pokegame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokegame.repository.PokemonRepository

class BattleViewModelFactory(private val repository: PokemonRepository) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BattleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BattleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
