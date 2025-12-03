package com.example.pokegame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.pokegame.repository.PokemonRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application, private val repository: PokemonRepository) : AndroidViewModel(application) {

    private val _downloadProgress = MutableLiveData<Int>(0)
    val downloadProgress: LiveData<Int> = _downloadProgress

    private val _downloadStatus = MutableLiveData<String>()
    val downloadStatus: LiveData<String> = _downloadStatus

    private val _estimatedSize = MutableLiveData<String>("Tamaño estimado: ~7.5 MB")
    val estimatedSize: LiveData<String> = _estimatedSize

    private val _isDownloading = MutableLiveData<Boolean>(false)
    val isDownloading: LiveData<Boolean> = _isDownloading

    fun startSpriteDownload() {
        if (_isDownloading.value == true) return

        // Immediate UI feedback
        _isDownloading.value = true
        _downloadStatus.value = "Conectando..."
        _downloadProgress.value = 0

        viewModelScope.launch {
            _downloadStatus.postValue("Obteniendo lista de Pokémon...")
            val pokemonResultList = repository.getPokemonResultList(151, 0)
            if (pokemonResultList.isEmpty()) {
                _downloadStatus.postValue("Error: No se pudo obtener la lista.")
                _isDownloading.postValue(false)
                return@launch
            }

            val totalPokemon = pokemonResultList.size
            var downloadedCount = 0
            _downloadStatus.postValue("Lista obtenida. Iniciando descargas...")

            pokemonResultList.forEach { pokeResult ->
                val id = pokeResult.url.split("/").dropLast(1).last().toInt()
                
                _downloadStatus.postValue("Preparando a ${pokeResult.name.uppercase()}...")
                val pokemonDetails = repository.getSinglePokemonDetails(id)

                if (pokemonDetails != null) {
                    try {
                        Glide.with(getApplication<Application>().applicationContext)
                            .downloadOnly()
                            .load(pokemonDetails.sprites.frontDefault)
                            .submit()
                        
                        downloadedCount++
                        val progress = (downloadedCount * 100 / totalPokemon)
                        _downloadProgress.postValue(progress)
                        _downloadStatus.postValue("Descargado: ${pokemonDetails.name.uppercase()} ($downloadedCount/$totalPokemon)")

                    } catch (e: Exception) {
                        _downloadStatus.postValue("Error al descargar ${pokemonDetails.name.uppercase()}")
                    }
                } else {
                    _downloadStatus.postValue("Error en detalles de ${pokeResult.name.uppercase()}")
                }
            }

            _downloadStatus.postValue("¡Descarga completada!")
            _isDownloading.postValue(false)
        }
    }
}