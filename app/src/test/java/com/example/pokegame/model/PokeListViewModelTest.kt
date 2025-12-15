package com.example.pokegame.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.pokegame.api.Pokemon
import com.example.pokegame.api.Species
import com.example.pokegame.repository.PokemonRepository
import com.example.pokegame.util.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokeListViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository = mockk<PokemonRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private lateinit var viewModel: PokeListViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PokeListViewModel(repository, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNextPage loads captured pokemons when user is logged in`() {
        // Given
        val observer = mockk<Observer<List<UiModel>>>(relaxed = true)
        viewModel.uiModels.observeForever(observer)

        every { sessionManager.getUsername() } returns "Ash"
        val mockPokemons =
            listOf(
                Pokemon(
                    id = 25,
                    name = "Pikachu",
                    baseExperience = 100,
                    height = 4,
                    isDefault = true,
                    order = 35,
                    weight = 60,
                    sprites = null,
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
            )
        coEvery { repository.getCapturedPokemons("Ash") } returns mockPokemons

        // When
        viewModel.loadNextPage()

        // Then
        // UiModels is MediatorLiveData depending on _pokemonList
        // It might take a moment or need verification of the internal state if exposed,
        // but since we rely on Observer:
        verify {
            observer.onChanged(
                    match { list ->
                        list.isNotEmpty() &&
                                list[0] is UiModel.PokemonItem &&
                                (list[0] as UiModel.PokemonItem).pokemon.name == "Pikachu"
                    }
            )
        }
    }

    @Test
    fun `loadNextPage returns empty list when user is NOT logged in`() {
        // Given
        every { sessionManager.getUsername() } returns null
        val observer = mockk<Observer<List<UiModel>>>(relaxed = true)
        viewModel.uiModels.observeForever(observer)

        // When
        viewModel.loadNextPage()

        // Then
        verify { observer.onChanged(match { it.isEmpty() }) }
        // Verify repository was NOT called
        coVerify(exactly = 0) { repository.getCapturedPokemons(any()) }
    }
}
