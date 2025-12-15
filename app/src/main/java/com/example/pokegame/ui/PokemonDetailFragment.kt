package com.example.pokegame.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pokegame.R
import com.example.pokegame.api.RetrofitClient
import com.example.pokegame.repository.PokemonRepository
import kotlin.math.sqrt
import kotlinx.coroutines.launch

class PokemonDetailFragment : Fragment(), SensorEventListener {

    private lateinit var imageView: ImageView
    private lateinit var nameView: TextView
    private lateinit var typesView: TextView
    private lateinit var miscView: TextView
    private lateinit var flavorView: TextView
    private lateinit var statsView: TextView
    private lateinit var progress: ProgressBar

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private var cryUrl: String? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pokemon_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.pokemon_image)
        nameView = view.findViewById(R.id.pokemon_name)
        typesView = view.findViewById(R.id.pokemon_types)
        miscView = view.findViewById(R.id.pokemon_misc)
        flavorView = view.findViewById(R.id.pokemon_flavor)
        statsView = view.findViewById(R.id.pokemon_stats)
        progress = view.findViewById(R.id.loading_progress)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val pokemonId = arguments?.getInt("pokemonId") ?: return

        loadPokemon(pokemonId)
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        releaseMediaPlayer()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
            if (gForce > 2.5f) { // Shake threshold
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 1000) { // Debounce 1s
                    lastShakeTime = now
                    playCry()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun playCry() {
        val url = cryUrl ?: return
        try {
            releaseMediaPlayer()
            mediaPlayer =
                    MediaPlayer().apply {
                        setAudioAttributes(
                                AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                        )
                        setDataSource(url)
                        prepareAsync()
                        setOnPreparedListener { start() }
                        setOnErrorListener { _, _, _ -> false }
                    }
            Toast.makeText(context, "Reproduciendo sonido...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun loadPokemon(id: Int) {
        progress.visibility = View.VISIBLE
        val repository =
                PokemonRepository(
                        RetrofitClient.service,
                        com.example.pokegame.api.UserRetrofitClient.getInstance(requireContext())
                )
        lifecycleScope.launch {
            try {
                val pokemon = repository.getSinglePokemonDetails(id)
                if (pokemon != null) {
                    // Name
                    nameView.text = (pokemon.name ?: "Desconocido").uppercase()

                    // Types
                    val typesText =
                            pokemon.types
                                    .map { it.type.name.uppercase() }
                                    .joinToString(", ")
                                    .takeIf { it.isNotEmpty() }
                                    ?: "—"
                    typesView.text = "Tipo: $typesText"

                    // Misc (legendary/mythical/location)
                    val miscText = StringBuilder()
                    if (pokemon.isLegendary) miscText.append("Legendario ")
                    if (pokemon.isMythical) miscText.append("Mítico ")

                    if (pokemon.latitude != null && pokemon.longitude != null) {
                        if (miscText.isNotEmpty()) miscText.append("\n")
                        miscText.append("Capturado en: ${pokemon.latitude}, ${pokemon.longitude}")
                    }
                    miscView.text = miscText.toString()

                    // Flavor text (prefer Spanish)
                    val flavorText =
                            if (pokemon.spanishFlavorTextEntries.isNotEmpty()) {
                                pokemon.spanishFlavorTextEntries.joinToString("\n\n")
                            } else {
                                pokemon.flavorTextEntries
                                        .find { it.language.name == "es" }
                                        ?.flavorText
                                        ?: pokemon.flavorTextEntries.firstOrNull()?.flavorText ?: ""
                            }
                    // Clean up special characters from flavor text
                    flavorView.text = flavorText.replace('\n', ' ').replace('\u000c', ' ')

                    // Stats
                    val statsText =
                            pokemon.stats.joinToString("\n") { stat ->
                                val statName = stat.stat.name.replace('-', ' ').uppercase()
                                "$statName: ${stat.baseStat}"
                            }
                    statsView.text = statsText

                    // Image
                    val imageUrl = pokemon.sprites?.frontDefault ?: ""
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_pokeball)
                            .error(R.drawable.ic_pokeball)
                            .into(imageView)

                    // Cry URL
                    cryUrl = pokemon.cries?.latest

                    // Setup Save Button
                    val saveButton = view?.findViewById<android.widget.Button>(R.id.save_button)
                    val sessionManager = com.example.pokegame.util.SessionManager(requireContext())

                    if (sessionManager.isLoggedIn()) {
                        saveButton?.visibility = View.VISIBLE
                        saveButton?.setOnClickListener {
                            savePokemonToCloud(pokemon, sessionManager.getUsername() ?: "")
                        }
                    } else {
                        saveButton?.visibility = View.GONE
                    }
                } else {
                    nameView.text = getString(R.string.app_name)
                    flavorView.text = getString(R.string.app_name)
                }
            } catch (e: Exception) {
                // Log the error for debugging, but don't crash the UI
                e.printStackTrace()
                nameView.text = getString(R.string.app_name)
                flavorView.text = getString(R.string.app_name)
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun savePokemonToCloud(pokemon: com.example.pokegame.api.Pokemon, username: String) {
        val repository =
                PokemonRepository(
                        RetrofitClient.service,
                        com.example.pokegame.api.UserRetrofitClient.getInstance(requireContext())
                )
        lifecycleScope.launch {
            Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
            val saved = repository.capturePokemon(username, pokemon)
            if (saved != null) {
                Toast.makeText(context, "¡Pokémon guardado en la nube!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
