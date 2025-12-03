package com.example.pokegame

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.pokegame.databinding.ActivityMainBinding
import com.example.pokegame.util.ThemeHelper
import com.example.pokegame.viewmodel.PokeballViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val pokeballViewModel: PokeballViewModel by viewModels()
    private var pokeballCountTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeHelper.getTheme(this))
        super.onCreate(savedInstanceState)
        com.example.pokegame.util.CapturedPokemonManager.init(this)

        // Apply Accessibility Settings
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean("large_text", false)) {
            val configuration = resources.configuration
            configuration.fontScale = 1.3f // 30% larger
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        // High Contrast is usually handled by theme attributes, but we can try a simple overlay or
        // theme switch
        // For now, let's assume High Contrast implies a specific theme or just system setting.
        // If we want to enforce it:
        if (prefs.getBoolean("high_contrast", false)) {
            // setTheme(R.style.Theme_Pokegame_HighContrast) // Assuming this style exists or we map
            // it
            // Since we don't have a HighContrast theme defined, let's just leave it as a
            // placeholder
            // or maybe force a dark theme with high contrast colors if we had one.
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Toolbar
        setSupportActionBar(binding.topAppBar)
        val appBarConfiguration =
                AppBarConfiguration(
                        setOf(
                                R.id.pokemonListFragment,
                                R.id.pokedexFragment,
                                R.id.captureFragment,
                                R.id.settingsFragment
                        )
                )
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Setup Bottom Navigation
        // NavigationUI.setupWithNavController(binding.bottomNav, navController) // Removed standard
        // setup

        binding.navShop.setOnClickListener { navController.navigate(R.id.shopFragment) }
        binding.navMyPokemons.setOnClickListener {
            navController.navigate(R.id.pokemonListFragment)
        }
        binding.btnCaptureCenter.setOnClickListener { navController.navigate(R.id.captureFragment) }
        binding.navFight.setOnClickListener {
            // Placeholder for Combat or Pokedex
            navController.navigate(R.id.pokedexFragment)
        }
        binding.navAccount.setOnClickListener { navController.navigate(R.id.settingsFragment) }

        // Observe Pokeball count
        pokeballViewModel.pokeballCount.observe(this) { updatePokeballCount(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        val menuItem = menu?.findItem(R.id.pokeballs_counter)
        val actionView = menuItem?.actionView
        pokeballCountTextView = actionView?.findViewById(R.id.pokeball_count)

        // Set a click listener on the whole action view
        actionView?.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(R.id.shopFragment)
        }

        // Set initial value
        pokeballViewModel.pokeballCount.value?.let { updatePokeballCount(it) }
        return true
    }

    private fun updatePokeballCount(count: Int) {
        pokeballCountTextView?.text = count.toString()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
