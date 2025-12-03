package com.example.pokegame.util

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {

    private val context = mockk<Context>()
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    @Test
    fun `saveAuthToken saves token to shared preferences`() {
        // Given
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        val sessionManager = SessionManager(context)

        // When
        sessionManager.saveAuthToken("test_token")

        // Then
        verify { editor.putString("auth_token", "test_token") }
        verify { editor.apply() }
    }

    @Test
    fun `isLoggedIn returns true when token exists`() {
        // Given
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.getString("auth_token", null) } returns "some_token"

        val sessionManager = SessionManager(context)

        // When
        val result = sessionManager.isLoggedIn()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isLoggedIn returns false when token is null`() {
        // Given
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.getString("auth_token", null) } returns null

        val sessionManager = SessionManager(context)

        // When
        val result = sessionManager.isLoggedIn()

        // Then
        assertFalse(result)
    }
}
