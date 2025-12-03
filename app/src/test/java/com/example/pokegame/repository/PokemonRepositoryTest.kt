package com.example.pokegame.repository

import com.example.pokegame.api.ApiService
import com.example.pokegame.api.PokeApiResponse
import com.example.pokegame.api.PokeResult
import com.example.pokegame.api.Pokemon
import com.example.pokegame.api.PokemonSprites
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonRepositoryTest {

    private val apiService = mockk<ApiService>()
    private val repository = PokemonRepository(apiService)

    @Test
    fun `getPokemonList returns list of pokemon when api call is successful`() = runTest {
        // Given
        val mockPokemonList =
                listOf(
                        PokeResult("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                        PokeResult("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
                )
        val mockResponse = PokeApiResponse(mockPokemonList)

        coEvery { apiService.getPokemonList(any(), any()) } returns mockResponse

        // Mock individual pokemon calls
        val mockPokemon1 =
                Pokemon(
                        1,
                        "bulbasaur",
                        10,
                        10,
                        emptyList(),
                        PokemonSprites("url"),
                        emptyList(),
                        emptyList(),
                        emptyList()
                )
        val mockPokemon2 =
                Pokemon(
                        2,
                        "ivysaur",
                        10,
                        10,
                        emptyList(),
                        PokemonSprites("url"),
                        emptyList(),
                        emptyList(),
                        emptyList()
                )

        coEvery { apiService.getPokemonInfo(1) } returns mockPokemon1
        coEvery { apiService.getPokemonSpecies(1) } returns mockk(relaxed = true)
        coEvery { apiService.getPokemonInfo(2) } returns mockPokemon2
        coEvery { apiService.getPokemonSpecies(2) } returns mockk(relaxed = true)

        // When
        val result = repository.getPokemonPage(2, 0)

        // Then
        assertEquals(2, result.size)
        assertEquals("bulbasaur", result[0].name)
        assertEquals("ivysaur", result[1].name)
    }

    @Test
    fun `getPokemonList returns empty list when api call fails`() = runTest {
        // Given
        coEvery { apiService.getPokemonList(any(), any()) } throws Exception("Network error")

        // When
        val result = repository.getPokemonPage(2, 0)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSinglePokemonDetails returns pokemon when api call is successful`() = runTest {
        // Given
        val mockPokemon =
                Pokemon(
                        25,
                        "pikachu",
                        10,
                        10,
                        emptyList(),
                        PokemonSprites("url"),
                        emptyList(),
                        emptyList(),
                        emptyList()
                )
        coEvery { apiService.getPokemonInfo(25) } returns mockPokemon
        coEvery { apiService.getPokemonSpecies(25) } returns mockk(relaxed = true)

        // When
        val result = repository.getSinglePokemonDetails(25)

        // Then
        assertEquals("pikachu", result?.name)
    }
}
