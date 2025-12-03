package com.example.pokegame.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.pokegame.repository.PokemonRepository
import io.mockk.coEvery
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
class SettingsViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val application = mockk<Application>(relaxed = true)
    private val repository = mockk<PokemonRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(application, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSpriteDownload updates status to downloading`() {
        // Given
        val observer = mockk<Observer<String>>(relaxed = true)
        viewModel.downloadStatus.observeForever(observer)

        coEvery { repository.getPokemonList() } returns emptyList()

        // When
        viewModel.startSpriteDownload()

        // Then
        verify { observer.onChanged(match { it.contains("Iniciando") }) }
    }
}
