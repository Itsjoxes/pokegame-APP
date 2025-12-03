package com.example.pokegame.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pokegame.api.Pokemon
import com.example.pokegame.repository.PokemonRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PokeListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PokeListViewModel
    private val repository: PokemonRepository = mockk()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNextPage should append new pokemon to the list`() = runTest {
        // Given - usa el testScheduler de runTest para el dispatcher main
        val mainDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(mainDispatcher)

        val page0 = listOf(Pokemon(1, "Bulbasaur", 64, 7, false, 1, 69, mockk(), mockk(), mockk(), mockk(), mockk(), "", mockk(), mockk(), mockk(), mockk(), emptyList()))
        val page1 = listOf(Pokemon(2, "Ivysaur", 142, 10, false, 2, 130, mockk(), mockk(), mockk(), mockk(), mockk(), "", mockk(), mockk(), mockk(), mockk(), emptyList()))

        coEvery { repository.getPokemonPage(20, 0) } returns page0
        coEvery { repository.getPokemonPage(20, 20) } returns page1

        // When
        viewModel = PokeListViewModel(repository) // init loads page 0
        // avanza el scheduler del runTest (que controla mainDispatcher)
        testScheduler.advanceUntilIdle()

        viewModel.loadNextPage() // Manually load page 1
        testScheduler.advanceUntilIdle()

        // Then
        val expectedPokemonItems = (page0 + page1).map { UiModel.PokemonItem(it) }
        val actualUiModels = viewModel.uiModels.value
        val actualPokemonItems = actualUiModels?.filterIsInstance<UiModel.PokemonItem>()

        assertEquals(expectedPokemonItems, actualPokemonItems)
    }
}
