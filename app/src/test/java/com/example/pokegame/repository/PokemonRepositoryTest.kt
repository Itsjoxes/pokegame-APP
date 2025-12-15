package com.example.pokegame.repository

import com.example.pokegame.api.ApiService
import com.example.pokegame.api.PokeApiResponse
import com.example.pokegame.api.PokeResult
import com.example.pokegame.api.Pokemon
import com.example.pokegame.api.Species
import com.example.pokegame.api.Sprites
import com.example.pokegame.api.UserService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonRepositoryTest {

    private val apiService = mockk<ApiService>()
    private val userService = mockk<UserService>()
    private val repository = PokemonRepository(apiService, userService)

    //Revisa la conexion a la api
    @Test
    fun `getPokemonList returns list of pokemon when api call is successful`() = runTest {
        // Given
        val mockPokemonList =
                listOf(
                        PokeResult("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                        PokeResult("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
                )
        val mockResponse = PokeApiResponse(1, "", "", mockPokemonList)

        coEvery { apiService.getPokemonList(any(), any()) } returns mockResponse

        // Mock individual pokemon calls
        val mockPokemon1 =
            Pokemon(
                id = 1,
                name = "bulbasaur",
                baseExperience = 10,
                height = 10,
                isDefault = true,
                order = 1,
                weight = 10,
                sprites = Sprites("url", null, null, null, null, null, null, null),
                abilities = emptyList(),
                forms = emptyList(),
                gameIndices = emptyList(),
                heldItems = emptyList(),
                locationAreaEncounters = "",
                moves = emptyList(),
                species = Species("bulbasaur", ""),
                stats = emptyList(),
                types = emptyList(),
                cries = null
            )
        val mockPokemon2 =
            Pokemon(
                id = 2,
                name = "ivysaur",
                baseExperience = 10,
                height = 10,
                isDefault = true,
                order = 2,
                weight = 20,
                sprites = Sprites("url", null, null, null, null, null, null, null),
                abilities = emptyList(),
                forms = emptyList(),
                gameIndices = emptyList(),
                heldItems = emptyList(),
                locationAreaEncounters = "",
                moves = emptyList(),
                species = Species("ivysaur", ""),
                stats = emptyList(),
                types = emptyList(),
                cries = null
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

    // Retornar una lista vacia cuando la llamada a la api falla

    @Test
    fun `getPokemonList returns empty list when api call fails`() = runTest {
        // Given
        coEvery { apiService.getPokemonList(any(), any()) } throws Exception("Network error")

        // When
        val result = repository.getPokemonPage(2, 0)

        // Then
        assertTrue(result.isEmpty())
    }

    // Retornar un pokemon cuando la llamada a la api es exitosa
    @Test
    fun `getSinglePokemonDetails returns pokemon when api call is successful`() = runTest {
        // Given
        val mockPokemon =
            Pokemon(
                id = 25,
                name = "pikachu",
                baseExperience = 10,
                height = 10,
                isDefault = true,
                order = 35,
                weight = 60,
                sprites = Sprites("url", null, null, null, null, null, null, null),
                abilities = emptyList(),
                forms = emptyList(),
                gameIndices = emptyList(),
                heldItems = emptyList(),
                locationAreaEncounters = "",
                moves = emptyList(),
                species = Species("pikachu", ""),
                stats = emptyList(),
                types = emptyList(),
                cries = null
            )
        coEvery { apiService.getPokemonInfo(25) } returns mockPokemon
        coEvery { apiService.getPokemonSpecies(25) } returns mockk(relaxed = true)

        // When
        val result = repository.getSinglePokemonDetails(25)

        // Then
        assertEquals("pikachu", result?.name)
    }
}
