package com.example.pokegame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokegame.repository.PokemonRepository

class BattleSelectionViewModelFactory(private val repository: PokemonRepository) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BattleSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BattleSelectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
