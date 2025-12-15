package com.example.pokegame.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokegame.R
import com.example.pokegame.api.RetrofitClient
import com.example.pokegame.databinding.FragmentPokemonListBinding
import com.example.pokegame.model.PokeListAdapter
import com.example.pokegame.model.PokeListViewModel
import com.example.pokegame.model.PokeListViewModelFactory
import com.example.pokegame.repository.PokemonRepository

class PokemonListFragment : Fragment() {

    private var _binding: FragmentPokemonListBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var adapter: PokeListAdapter
    private lateinit var viewModel: PokeListViewModel
    private lateinit var errorMessage: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var errorRunnable: Runnable? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokemonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository =
                PokemonRepository(
                        RetrofitClient.service,
                        com.example.pokegame.api.UserRetrofitClient.getInstance(requireContext())
                )
        val sessionManager = com.example.pokegame.util.SessionManager(requireContext())
        val factory = PokeListViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[PokeListViewModel::class.java]

        setupRecyclerView()

        // init local reference for error message because view-binding
        // generation can sometimes omit views across layout variants
        val resId = resources.getIdentifier("error_message", "id", requireContext().packageName)
        errorMessage = binding.root.findViewById<TextView>(resId)!!

        observeViewModel()

        // Show error message if loading takes too long
        errorRunnable =
                Runnable {
                    if (adapter.itemCount == 0) {
                        binding.loadingOverlay.visibility = View.GONE
                        binding.loadingProgress.visibility = View.GONE
                        errorMessage.visibility = View.VISIBLE
                    }
                }
                        .also { handler.postDelayed(it, 25000) }

        // Load initial data
        viewModel.loadNextPage()
    }

    private fun setupRecyclerView() {
        adapter = PokeListAdapter { id ->
            val args = bundleOf("pokemonId" to id)
            findNavController()
                    .navigate(R.id.action_pokemonListFragment_to_pokemonDetailFragment, args)
        }

        val layoutManager = GridLayoutManager(requireContext(), 2)

        // loading item ocupa 2 columnas
        layoutManager.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter.getItemViewType(position) == 1) 2 else 1
                    }
                }

        binding.pokemonRecycler.layoutManager = layoutManager
        binding.pokemonRecycler.adapter = adapter

        binding.pokemonRecycler.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        // Only trigger when user scrolls downwards to avoid immediate
                        // auto-loading after submitList() which can cause repeated loads
                        // and visual flicker.
                        if (dy <= 0) return

                        val lastVisible = layoutManager.findLastVisibleItemPosition()
                        val total = layoutManager.itemCount
                        val threshold = 4

                        val isLoading = viewModel.isLoading.value ?: false

                        if (!isLoading && lastVisible >= total - threshold) {
                            viewModel.loadNextPage()
                        }
                    }
                }
        )
    }

    private fun observeViewModel() {
        // lista de ui models
        viewModel.uiModels.observe(viewLifecycleOwner) { uiModels ->
            adapter.submitList(uiModels)
            // If data is loaded, remove the error message callback
            if (uiModels.isNotEmpty()) {
                errorRunnable?.let { handler.removeCallbacks(it) }
                errorMessage.visibility = View.GONE
            }
        }

        // loading
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // Do not show loading overlay if it's the initial load to prevent flicker
            if (adapter.itemCount > 0) {
                binding.loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
                binding.loadingProgress.visibility = if (loading) View.VISIBLE else View.GONE
            }
            if (loading) {
                errorMessage.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove callbacks to prevent memory leaks
        errorRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
