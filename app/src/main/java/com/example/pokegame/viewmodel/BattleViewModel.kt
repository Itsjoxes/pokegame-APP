package com.example.pokegame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokegame.api.Pokemon
import com.example.pokegame.repository.PokemonRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BattleViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _playerPokemon = MutableLiveData<Pokemon>()
    val playerPokemon: LiveData<Pokemon> = _playerPokemon

    private val _rivalPokemon = MutableLiveData<Pokemon>()
    val rivalPokemon: LiveData<Pokemon> = _rivalPokemon

    private val _playerHp = MutableLiveData<Int>()
    val playerHp: LiveData<Int> = _playerHp

    private val _rivalHp = MutableLiveData<Int>()
    val rivalHp: LiveData<Int> = _rivalHp

    private val _battleLog = MutableLiveData<String>()
    val battleLog: LiveData<String> = _battleLog

    private val _isPlayerTurn = MutableLiveData<Boolean>(true)
    val isPlayerTurn: LiveData<Boolean> = _isPlayerTurn

    private val _battleState = MutableLiveData<BattleState>(BattleState.Ongoing)
    val battleState: LiveData<BattleState> = _battleState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun initBattle(playerPokemonId: Int, rivalPokemonId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            // Artificial delay for effect (optional, but good for "console" feel)
            delay(1500)

            val player = repository.getSinglePokemonDetails(playerPokemonId)
            val rival = repository.getSinglePokemonDetails(rivalPokemonId)

            if (player != null && rival != null) {
                _playerPokemon.value = player
                _rivalPokemon.value = rival

                // Simplified HP calculation: Base HP * 2 + 110 (Level 50 approx)
                // For now just use Base Stat * 3 for gameplay duration
                val playerMaxHp = (player.stats.find { it.stat.name == "hp" }?.baseStat ?: 50) * 3
                val rivalMaxHp = (rival.stats.find { it.stat.name == "hp" }?.baseStat ?: 50) * 3

                _playerHp.value = playerMaxHp
                _rivalHp.value = rivalMaxHp

                _battleLog.value = "¡Un ${rival.name} salvaje apareció!"
            }
            _isLoading.value = false
        }
    }

    fun performAttack(moveIndex: Int) {
        if (_isPlayerTurn.value == false || _battleState.value != BattleState.Ongoing) return

        _isPlayerTurn.value = false
        val player = _playerPokemon.value ?: return
        val rival = _rivalPokemon.value ?: return

        // Simplified damage logic
        val moveName =
                if (player.moves.size > moveIndex) player.moves[moveIndex].move.name
                else "Ataque Rápido"
        val damage = calculateDamage(player, rival)

        _battleLog.value = "${player.name} usó $moveName!"

        viewModelScope.launch {
            delay(1000)
            val newHp = (_rivalHp.value ?: 0) - damage
            _rivalHp.value = newHp.coerceAtLeast(0)
            _battleLog.value = "¡Hizo $damage de daño!"

            if (newHp <= 0) {
                _battleState.value = BattleState.Won
                _battleLog.value = "¡${rival.name} se debilitó! ¡Ganaste!"
            } else {
                delay(1500)
                enemyTurn()
            }
        }
    }

    private fun enemyTurn() {
        val player = _playerPokemon.value ?: return
        val rival = _rivalPokemon.value ?: return

        _battleLog.value = "${rival.name} ataca!"

        viewModelScope.launch {
            delay(1000)
            val damage = calculateDamage(rival, player)
            val newHp = (_playerHp.value ?: 0) - damage
            _playerHp.value = newHp.coerceAtLeast(0)
            _battleLog.value = "¡${rival.name} hizo $damage de daño!"

            if (newHp <= 0) {
                _battleState.value = BattleState.Lost
                _battleLog.value = "¡${player.name} se debilitó! Perdiste..."
            } else {
                _isPlayerTurn.value = true
                delay(1000)
                _battleLog.value = "¿Qué hará ${player.name}?"
            }
        }
    }

    private fun calculateDamage(attacker: Pokemon, defender: Pokemon): Int {
        // Very simplified formula
        val attack = attacker.stats.find { it.stat.name == "attack" }?.baseStat ?: 50
        val defense = defender.stats.find { it.stat.name == "defense" }?.baseStat ?: 50

        // Random variance 0.85 to 1.0
        val variance = (85..100).random() / 100.0

        val damage = (((2 * 50 / 5 + 2) * attack * 60 / defense) / 50 + 2) * variance
        return damage.toInt().coerceAtLeast(1)
    }

    enum class BattleState {
        Ongoing,
        Won,
        Lost
    }
}
