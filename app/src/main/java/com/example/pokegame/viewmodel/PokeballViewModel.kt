package com.example.pokegame.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PokeballViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences =
            application.getSharedPreferences("pokegame_prefs", Context.MODE_PRIVATE)
    private val _pokeballCount = MutableLiveData<Int>()
    val pokeballCount: LiveData<Int> = _pokeballCount

    init {
        _pokeballCount.value = sharedPreferences.getInt("pokeball_count", 0)
    }

    fun addPokeballs(amount: Int) {
        val current = _pokeballCount.value ?: 0
        val newCount = current + amount
        _pokeballCount.value = newCount
        sharedPreferences.edit().putInt("pokeball_count", newCount).apply()
    }

    fun spendPokeballs(amount: Int): Boolean {
        val current = _pokeballCount.value ?: 0
        if (current >= amount) {
            val newCount = current - amount
            _pokeballCount.value = newCount
            sharedPreferences.edit().putInt("pokeball_count", newCount).apply()
            return true
        }
        return false
    }
}
