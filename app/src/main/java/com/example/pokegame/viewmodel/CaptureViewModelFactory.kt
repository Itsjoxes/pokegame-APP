package com.example.pokegame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokegame.repository.PokemonRepository

class CaptureViewModelFactory(private val repository: PokemonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaptureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaptureViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}