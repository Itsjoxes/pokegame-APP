package com.example.pokegame.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokegame.api.Pokemon
import com.example.pokegame.repository.PokemonRepository
import kotlinx.coroutines.launch

class PokeListViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _pokemonList = MutableLiveData<List<Pokemon>>(emptyList())
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    val uiModels: LiveData<List<UiModel>> =
            MediatorLiveData<List<UiModel>>().apply {
                addSource(_pokemonList) { pokemon ->
                    value = buildUiModels(pokemon, _isLoading.value ?: false)
                }
                addSource(_isLoading) { isLoading ->
                    value = buildUiModels(_pokemonList.value ?: emptyList(), isLoading)
                }
            }

    fun loadNextPage() {
        if (_isLoading.value == true) return

        _isLoading.value = true

        viewModelScope.launch {
            // Load ONLY captured pokemons
            val capturedIds =
                    com.example.pokegame.util.CapturedPokemonManager.getCapturedIds().map {
                        it.toInt()
                    }

            if (capturedIds.isEmpty()) {
                _pokemonList.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            val newPokemon = repository.getPokemonListByIds(capturedIds)
            _pokemonList.value = newPokemon
            _isLoading.value = false
        }
    }

    private fun buildUiModels(pokemon: List<Pokemon>, isLoading: Boolean): List<UiModel> {
        val models = pokemon.map { UiModel.PokemonItem(it) }.toMutableList<UiModel>()
        return models
    }
}
