package com.example.pokegame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pokegame.R
import com.example.pokegame.api.RetrofitClient
import com.example.pokegame.databinding.FragmentPokedexBinding
import com.example.pokegame.model.BattleSelectionAdapter
import com.example.pokegame.repository.PokemonRepository
import com.example.pokegame.viewmodel.BattleSelectionViewModel
import com.example.pokegame.viewmodel.BattleSelectionViewModelFactory

class PokedexFragment : Fragment() {

    private var _binding: FragmentPokedexBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: BattleSelectionViewModel
    private lateinit var adapter: BattleSelectionAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokedexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userService = com.example.pokegame.api.UserRetrofitClient.getInstance(requireContext())
        val repository = PokemonRepository(RetrofitClient.service, userService)
        val factory = BattleSelectionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[BattleSelectionViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = BattleSelectionAdapter { pokemon ->
            android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Iniciando Batalla")
                    .setMessage(
                            "Cargando entorno de batalla...\nNota: Algunos sprites pueden tardar en procesarse correctamente."
                    )
                    .setPositiveButton("Continuar") { _, _ ->
                        val args = bundleOf("rivalId" to pokemon.id)
                        findNavController()
                                .navigate(R.id.action_pokedexFragment_to_battleFragment, args)
                    }
                    .show()
        }
        binding.battleRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.battleRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.battleCandidates.observe(viewLifecycleOwner) { candidates ->
            adapter.submitList(candidates)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgress.isVisible = isLoading
            binding.battleRecycler.isVisible = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
