package com.example.pokegame.repository

import android.util.Log
import com.example.pokegame.api.ApiService
import com.example.pokegame.api.PokeResult
import com.example.pokegame.api.Pokemon
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

class PokemonRepository(
        private val apiService: ApiService,
        private val userService: com.example.pokegame.api.UserService
) {

    // In-memory cache for Pokemon keyed by id to avoid relying on generated hashCode()
    private val pokemonCache = mutableMapOf<Int, Pokemon>()

    suspend fun getPokemonPage(limit: Int, offset: Int): List<Pokemon> {
        return try {
            coroutineScope {
                val pokeApiResponse = apiService.getPokemonList(limit, offset)
                pokeApiResponse.results
                        .map { pokeResult ->
                            async {
                                getSinglePokemon(
                                        pokeResult.url.split("/").dropLast(1).last().toInt()
                                )
                            }
                        }
                        .mapNotNull { it.await() }
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error loading pokemon page", e)
            emptyList<Pokemon>()
        }
    }

    private suspend fun getSinglePokemon(id: Int): Pokemon? {
        // Return cached pokemon if it exists
        val cached = pokemonCache[id]
        if (cached != null) {
            return cached
        }
        // otherwise, load from the api and cache it
        return getSinglePokemonDetails(id)?.also { pokemonCache[it.id] = it }
    }

    suspend fun getPokemonList(): List<Pokemon> {
        return try {
            getPokemonPage(151, 0) // Just get the first 151 Pokemon
        } catch (e: HttpException) {
            Log.e("PokemonRepository", "HTTP error getting pokemon list", e)
            if (e.code() == 429 && pokemonCache.isNotEmpty()) {
                pokemonCache.values.toList().shuffled().take(20)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Unknown error getting pokemon list", e)
            emptyList()
        }
    }

    suspend fun getPokemonResultList(limit: Int, offset: Int): List<PokeResult> {
        return try {
            apiService.getPokemonList(limit, offset).results
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error getting Pokemon results", e)
            emptyList()
        }
    }

    suspend fun getSinglePokemonDetails(id: Int): Pokemon? {
        Log.d("PokemonRepository", "Getting details for id: $id")
        return try {
            coroutineScope {
                val pokemonInfoDeferred = async {
                    try {
                        apiService.getPokemonInfo(id)
                    } catch (e: Exception) {
                        Log.e("PokemonRepository", "Start info fail", e)
                        throw e
                    }
                }
                val speciesInfoDeferred = async {
                    try {
                        apiService.getPokemonSpecies(id)
                    } catch (e: Exception) {
                        Log.e("PokemonRepository", "Start species fail", e)
                        throw e
                    }
                }

                val pokemonInfo = pokemonInfoDeferred.await()
                Log.d("PokemonRepository", "Got pokemon info for $id")

                try {
                    val speciesInfo = speciesInfoDeferred.await()
                    Log.d("PokemonRepository", "Got species info for $id")
                    pokemonInfo.apply {
                        isLegendary = speciesInfo.isLegendary
                        isMythical = speciesInfo.isMythical
                        spanishFlavorTextEntries =
                                speciesInfo.flavorTextEntries
                                        .filter { it.language.name == "es" }
                                        .map { it.flavorText }
                    }
                } catch (e: Exception) {
                    Log.w(
                            "PokemonRepository",
                            "Failed to get species info, continuing only with pokemon info",
                            e
                    )
                    // Allow continuing without species info if that fails (it's secondary)
                }

                // POPULATE LOCAL CAPTURE LOCATION IF AVAILABLE
                val location =
                        com.example.pokegame.util.CapturedPokemonManager.getCaptureLocation(id)
                if (location != null) {
                    pokemonInfo.latitude = location.first
                    pokemonInfo.longitude = location.second
                }

                pokemonInfo
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error getting single pokemon details for id=$id", e)
            null
        }
    }
    suspend fun getPokemonListByIds(ids: List<Int>): List<Pokemon> {
        return try {
            coroutineScope {
                ids.map { id -> async { getSinglePokemon(id) } }.mapNotNull { it.await() }
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error getting pokemon list by ids", e)
            emptyList()
        }
    }

    suspend fun capturePokemon(username: String, pokemon: Pokemon): Pokemon? {
        return try {
            // Ensure urlImagen is set before sending
            if (pokemon.urlImagen == null) {
                pokemon.urlImagen = pokemon.sprites?.frontDefault
            }

            val response = userService.capturePokemon(username, pokemon)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("PokemonRepository", "Error capturing pokemon: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error capturing pokemon", e)
            null
        }
    }

    suspend fun getCapturedPokemons(username: String): List<Pokemon> {
        Log.d("PokemonRepository", "Requesting captured pokemons for user: $username")
        return try {
            val response = userService.getCapturedPokemons(username)
            Log.d("PokemonRepository", "Response code: ${response.code()}")
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d("PokemonRepository", "Found ${list.size} captured pokemons")
                list
            } else {
                Log.e(
                        "PokemonRepository",
                        "Error getting captured pokemons: ${response.code()} ${response.errorBody()?.string()}"
                )
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error getting captured pokemons", e)
            emptyList()
        }
    }
}
