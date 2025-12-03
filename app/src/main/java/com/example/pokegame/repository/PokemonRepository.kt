package com.example.pokegame.repository

import android.util.Log
import com.example.pokegame.api.ApiService
import com.example.pokegame.api.PokeResult
import com.example.pokegame.api.Pokemon
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

class PokemonRepository(private val apiService: ApiService) {

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
        return try {
            coroutineScope {
                val pokemonInfoDeferred = async { apiService.getPokemonInfo(id) }
                val speciesInfoDeferred = async { apiService.getPokemonSpecies(id) }

                val pokemonInfo = pokemonInfoDeferred.await()
                val speciesInfo = speciesInfoDeferred.await()

                pokemonInfo.apply {
                    isLegendary = speciesInfo.isLegendary
                    isMythical = speciesInfo.isMythical
                }
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
                pokemon.urlImagen = pokemon.sprites.frontDefault
            }

            // We need a UserService instance.
            // Since PokemonRepository currently only has ApiService, we might need to refactor or
            // pass UserService.
            // However, checking the codebase, there is a UserRetrofitClient.
            // For now, let's assume we can access it or the user should pass the service.
            // Wait, the plan didn't specify refactoring the Repository constructor.
            // Let's check if we can use UserRetrofitClient directly or if we should add it to the
            // constructor.
            // Given the context, it's cleaner to add it to the constructor or use a singleton if
            // available.
            // But to avoid breaking changes in other files, let's use UserRetrofitClient.service
            // directly here if possible,
            // or better, just add the method and let the ViewModel handle the service call?
            // No, the repository should handle data operations.

            // Let's use UserRetrofitClient.service directly for now to minimize impact,
            // or better yet, just add the function and call the service.
            val response =
                    com.example.pokegame.api.UserRetrofitClient.service?.capturePokemon(
                            username,
                            pokemon
                    )
            if (response != null && response.isSuccessful) {
                response.body()
            } else {
                Log.e("PokemonRepository", "Error capturing pokemon: ${response?.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error capturing pokemon", e)
            null
        }
    }
}
