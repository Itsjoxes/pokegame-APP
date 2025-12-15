package com.example.pokegame.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokegame.api.Pokemon
import com.example.pokegame.repository.PokemonRepository
import kotlinx.coroutines.launch

class PokeListViewModel(
        private val repository: PokemonRepository,
        private val sessionManager: com.example.pokegame.util.SessionManager
) : ViewModel() {

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
            // Priority 1: Load from Local Storage (Offline-ish capability) to satisfy "my captured
            // pokemons"
            val localIdsStrings = com.example.pokegame.util.CapturedPokemonManager.getCapturedIds()
            val localIds = localIdsStrings.mapNotNull { it.toIntOrNull() }

            android.util.Log.d("PokeListViewModel", "Local captured IDs: $localIds")

            // Fetch details for these IDs using PokeAPI (repository handles caching potentially)
            val uniqueLocalPokemon =
                    if (localIds.isNotEmpty()) {
                        repository.getPokemonListByIds(localIds)
                    } else {
                        emptyList()
                    }

            // Priority 2: Try Backend (Sync)
            val username = sessionManager.getUsername()
            var backendPokemon: List<Pokemon> = emptyList()
            if (username != null) {
                try {
                    val rawBackend = repository.getCapturedPokemons(username)

                    // Healing corrupt data (null names)
                    val corruptIds = rawBackend.filter { it.name == null }.map { it.id }
                    if (corruptIds.isNotEmpty()) {
                        android.util.Log.d(
                                "PokeListViewModel",
                                "Repairing ${corruptIds.size} corrupted backend pokemons"
                        )
                        val repaired = repository.getPokemonListByIds(corruptIds)

                        backendPokemon =
                                rawBackend.map { original ->
                                    repaired.find { it.id == original.id } ?: original
                                }
                    } else {
                        backendPokemon = rawBackend
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PokeListViewModel", "Failed to fetch backend captures", e)
                }
            }

            // Combine lists (Deduplicate by ID)
            val combined = (uniqueLocalPokemon + backendPokemon).distinctBy { it.id }

            android.util.Log.d("PokeListViewModel", "Total unique pokemons: ${combined.size}")
            _pokemonList.value = combined
            _isLoading.value = false
        }
    }

    private fun buildUiModels(pokemon: List<Pokemon>, isLoading: Boolean): List<UiModel> {
        val models = pokemon.map { UiModel.PokemonItem(it) }.toMutableList<UiModel>()
        return models
    }
}
