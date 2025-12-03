package com.example.pokegame.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pokegame.api.RetrofitClient
import com.example.pokegame.databinding.FragmentBattleBinding
import com.example.pokegame.repository.PokemonRepository
import com.example.pokegame.viewmodel.BattleViewModel
import com.example.pokegame.viewmodel.BattleViewModelFactory

class BattleFragment : Fragment() {

    private var _binding: FragmentBattleBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: BattleViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pokeballViewModel =
                ViewModelProvider(requireActivity())[
                        com.example.pokegame.viewmodel.PokeballViewModel::class.java]

        // Check balance and deduct cost
        if (!pokeballViewModel.spendPokeballs(10)) {
            android.widget.Toast.makeText(
                            context,
                            "No tienes suficientes Pokeballs (10 requeridas)",
                            android.widget.Toast.LENGTH_LONG
                    )
                    .show()
            findNavController().popBackStack()
            return
        }

        val repository = PokemonRepository(RetrofitClient.service)
        val factory = BattleViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[BattleViewModel::class.java]

        val rivalId = arguments?.getInt("rivalId") ?: return
        // For now, pick the first captured pokemon as player pokemon, or a default one if none
        val playerIds = com.example.pokegame.util.CapturedPokemonManager.getCapturedIds().toList()
        val playerId =
                if (playerIds.isNotEmpty()) playerIds.first().toInt() else 25 // Default Pikachu

        viewModel.initBattle(playerId, rivalId)

        setupObservers()
        setupButtons()
    }

    private fun setupButtons() {
        val buttons = listOf(binding.move1, binding.move2, binding.move3)
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { viewModel.performAttack(index) }
        }

        binding.move4.setOnClickListener {
            // Fleeing counts as loss
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.playerPokemon.observe(viewLifecycleOwner) { pokemon ->
            binding.playerName.text = pokemon.name.uppercase()

            // Set color based on first type
            val type = pokemon.types.firstOrNull()?.type?.name ?: "normal"
            binding.playerName.setTextColor(
                    com.example.pokegame.util.TypeColorHelper.getColorForType(type)
            )

            Glide.with(this)
                    .load(pokemon.sprites.backDefault ?: pokemon.sprites.frontDefault)
                    .into(binding.playerImage)

            // Set move names
            val moves = pokemon.moves.take(3)
            val buttons = listOf(binding.move1, binding.move2, binding.move3)
            buttons.forEachIndexed { index, button ->
                if (index < moves.size) {
                    button.text = moves[index].move.name.uppercase()
                } else {
                    button.text = "-"
                    button.isEnabled = false
                }
            }
        }

        viewModel.rivalPokemon.observe(viewLifecycleOwner) { pokemon ->
            binding.rivalName.text = pokemon.name.uppercase()

            // Set color based on first type
            val type = pokemon.types.firstOrNull()?.type?.name ?: "normal"
            binding.rivalName.setTextColor(
                    com.example.pokegame.util.TypeColorHelper.getColorForType(type)
            )

            Glide.with(this).load(pokemon.sprites.frontDefault).into(binding.rivalImage)
        }

        viewModel.playerHp.observe(viewLifecycleOwner) { hp ->
            binding.playerHpBar.max =
                    (viewModel.playerPokemon.value?.stats?.find { it.stat.name == "hp" }?.baseStat
                            ?: 50) * 3
            binding.playerHpBar.progress = hp
            binding.playerHpText.text = "$hp/${binding.playerHpBar.max}"
        }

        viewModel.rivalHp.observe(viewLifecycleOwner) { hp ->
            binding.rivalHpBar.max =
                    (viewModel.rivalPokemon.value?.stats?.find { it.stat.name == "hp" }?.baseStat
                            ?: 50) * 3
            binding.rivalHpBar.progress = hp
        }

        viewModel.battleLog.observe(viewLifecycleOwner) { log -> binding.battleLog.text = log }

        viewModel.isPlayerTurn.observe(viewLifecycleOwner) { isTurn ->
            binding.movesGrid.isEnabled = isTurn
            binding.move1.isEnabled = isTurn
            binding.move2.isEnabled = isTurn
            binding.move3.isEnabled = isTurn
        }

        viewModel.battleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BattleViewModel.BattleState.Won ->
                        showEndDialog("¡Ganaste!", "Has derrotado al rival.")
                BattleViewModel.BattleState.Lost ->
                        showEndDialog("Perdiste", "Tu Pokémon se ha debilitado.")
                else -> {}
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showEndDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Salir") { _, _ -> findNavController().popBackStack() }
                .setCancelable(false)
                .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
