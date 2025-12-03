package com.example.pokegame.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokegame.repository.PokemonRepository

class PokeListViewModelFactory(private val repository: PokemonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokeListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokeListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}