package com.example.pokegame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokegame.api.Pokemon
import com.example.pokegame.repository.PokemonRepository
import kotlinx.coroutines.launch

class BattleSelectionViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _battleCandidates = MutableLiveData<List<Pokemon>>()
    val battleCandidates: LiveData<List<Pokemon>> = _battleCandidates

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadCandidates()
    }

    fun loadCandidates() {
        _isLoading.value = true
        viewModelScope.launch {
            // Generate 10 random IDs between 1 and 1000
            val randomIds = (1..1000).shuffled().take(10)
            val pokemons = repository.getPokemonListByIds(randomIds)
            _battleCandidates.value = pokemons
            _isLoading.value = false
        }
    }
}
