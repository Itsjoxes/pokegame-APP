package com.example.pokegame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokegame.api.Pokemon
import com.example.pokegame.repository.PokemonRepository
import kotlinx.coroutines.launch

class CaptureViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _wildPokemon = MutableLiveData<List<Pokemon>>()
    val wildPokemon: LiveData<List<Pokemon>> = _wildPokemon

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        refreshWildPokemon()
    }

    fun refreshWildPokemon() {
        _isLoading.value = true
        viewModelScope.launch {
            // Generate 20 random IDs between 1 and 1000
            val randomIds = (1..1000).shuffled().take(20)
            val pokemons = repository.getPokemonListByIds(randomIds)
            _wildPokemon.value = pokemons
            _isLoading.value = false
        }
    }

    fun attemptCapture(pokemon: Pokemon, currentPokeballs: Int): Boolean {
        if (currentPokeballs <= 0) {
            return false // No pokeballs
        }

        val captured = Math.random() > 0.5 // 50% chance of capture

        if (captured) {
            com.example.pokegame.util.CapturedPokemonManager.addCaptured(pokemon.id)
        }

        // Remove the Pokemon from the list whether it was captured or not
        val currentList = _wildPokemon.value?.toMutableList() ?: mutableListOf()
        currentList.remove(pokemon)
        _wildPokemon.value = currentList

        return captured
    }
}
