package com.example.pokegame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.pokegame.databinding.FragmentShopBinding
import com.example.pokegame.viewmodel.PokeballViewModel

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding
        get() = _binding!!

    private val pokeballViewModel: PokeballViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buyPokeballsButton.setOnClickListener { pokeballViewModel.addPokeballs(10) }

        binding.buy50PokeballsButton.setOnClickListener { pokeballViewModel.addPokeballs(50) }

        binding.buy100PokeballsButton.setOnClickListener { pokeballViewModel.addPokeballs(100) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
