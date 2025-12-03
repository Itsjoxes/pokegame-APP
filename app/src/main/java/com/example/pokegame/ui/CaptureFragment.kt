package com.example.pokegame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pokegame.api.RetrofitClient
import com.example.pokegame.databinding.FragmentCaptureBinding
import com.example.pokegame.model.CaptureAdapter
import com.example.pokegame.repository.PokemonRepository
import com.example.pokegame.viewmodel.CaptureViewModel
import com.example.pokegame.viewmodel.CaptureViewModelFactory
import com.example.pokegame.viewmodel.PokeballViewModel

class CaptureFragment : Fragment() {

    private var _binding: FragmentCaptureBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var captureViewModel: CaptureViewModel
    private val pokeballViewModel: PokeballViewModel by activityViewModels()
    private lateinit var adapter: CaptureAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = PokemonRepository(RetrofitClient.service)
        val factory = CaptureViewModelFactory(repository)
        captureViewModel = ViewModelProvider(this, factory).get(CaptureViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        binding.refreshButton.setOnClickListener { captureViewModel.refreshWildPokemon() }
    }

    private fun setupRecyclerView() {
        adapter = CaptureAdapter { pokemon ->
            val currentPokeballs = pokeballViewModel.pokeballCount.value ?: 0
            val cost = (pokemon.baseExperience / 20).coerceAtLeast(1)

            if (currentPokeballs >= cost) {
                val captured = captureViewModel.attemptCapture(pokemon, currentPokeballs)
                pokeballViewModel.spendPokeballs(
                        cost
                ) // Deduct cost regardless of outcome (attempt cost)

                val message = if (captured) "¡${pokemon.name} capturado!" else "¡Oh no, se escapó!"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                                requireContext(),
                                "¡No tienes suficientes Pokébolas! (Req: $cost)",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
        binding.captureRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.captureRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        captureViewModel.wildPokemon.observe(viewLifecycleOwner) { adapter.submitList(it) }

        captureViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.isVisible = isLoading
            binding.contentContainer.isVisible = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
