package com.example.pokegame.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.pokegame.R
import com.example.pokegame.api.RetrofitClient
import com.example.pokegame.repository.PokemonRepository
import com.example.pokegame.util.SessionManager
import com.example.pokegame.util.ThemeHelper
import com.example.pokegame.viewmodel.SettingsViewModel
import com.example.pokegame.viewmodel.SettingsViewModelFactory

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        sessionManager = SessionManager(requireContext())

        val application = requireActivity().application
        val userService = com.example.pokegame.api.UserRetrofitClient.getInstance(requireContext())
        val repository = PokemonRepository(RetrofitClient.service, userService)
        val factory = SettingsViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        setupAccountPreference()

        // Download preference
        val downloadPreference: Preference? = findPreference("download_sprites")
        downloadPreference?.setOnPreferenceClickListener {
            viewModel.startSpriteDownload()
            true
        }

        // View resources preference
        val viewResourcesPreference: Preference? = findPreference("view_resources")
        viewResourcesPreference?.setOnPreferenceClickListener {
            val cacheDir = requireContext().cacheDir
            val imageCacheDir = java.io.File(cacheDir, "image_manager_disk_cache") // Glide default
            val size = getDirSize(imageCacheDir)
            val sizeMb = size / (1024 * 1024)

            AlertDialog.Builder(requireContext())
                    .setTitle("Recursos Descargados")
                    .setMessage("Tamaño de caché de imágenes: ${sizeMb}MB\n(Aproximado)")
                    .setPositiveButton("OK", null)
                    .show()
            true
        }

        // Theme preference
        val themePreference: ListPreference? = findPreference("app_theme")
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            ThemeHelper.setTheme(requireContext(), theme)
            requireActivity().recreate() // Recreate the activity to apply the new theme
            true
        }

        // High Contrast preference
        val highContrastPreference: androidx.preference.SwitchPreferenceCompat? =
                findPreference("high_contrast")
        highContrastPreference?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            ThemeHelper.setHighContrast(requireContext(), enabled)
            requireActivity().recreate()
            true
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        setupAccountPreference() // Refresh state when returning from LoginFragment
    }

    private fun setupAccountPreference() {
        val accountPreference: Preference? = findPreference("account_action")
        if (sessionManager.isLoggedIn()) {
            val username = sessionManager.getUsername() ?: "Usuario"
            accountPreference?.title = "Cerrar Sesión"
            accountPreference?.summary = "Sesión iniciada como $username"
            accountPreference?.setOnPreferenceClickListener {
                showLogoutConfirmation()
                true
            }
        } else {
            accountPreference?.title = "Iniciar Sesión"
            accountPreference?.summary = "Inicia sesión o regístrate para guardar tu progreso."
            accountPreference?.setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
                true
            }
        }

        // Profile Picture Logic
        val profilePicPreference: Preference? = findPreference("profile_picture")
        profilePicPreference?.setOnPreferenceClickListener {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                takePictureLauncher.launch(null)
            } else {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            true
        }
    }

    private val takePictureLauncher =
            registerForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
            ) { bitmap ->
                if (bitmap != null) {
                    // Save bitmap to internal storage
                    try {
                        val filename = "profile_pic.png"
                        val stream =
                                requireContext()
                                        .openFileOutput(
                                                filename,
                                                android.content.Context.MODE_PRIVATE
                                        )
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                        stream.close()
                        Toast.makeText(
                                        requireContext(),
                                        "¡Foto de perfil guardada!",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                                        requireContext(),
                                        "Error al guardar la foto",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                }
            }

    private val requestCameraPermissionLauncher =
            registerForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    takePictureLauncher.launch(null)
                } else {
                    Toast.makeText(
                                    requireContext(),
                                    "Permiso de cámara necesario",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    // Clear Session
                    sessionManager.clearSession()

                    // Clear Captured Pokemon (Local)
                    com.example.pokegame.util.CapturedPokemonManager.clear()

                    // Clear Pokeballs
                    requireContext()
                            .getSharedPreferences(
                                    "pokegame_prefs",
                                    android.content.Context.MODE_PRIVATE
                            )
                            .edit()
                            .clear()
                            .apply()

                    Toast.makeText(
                                    requireContext(),
                                    "Sesión cerrada y datos locales borrados",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    setupAccountPreference()
                }
                .setNegativeButton("No", null)
                .show()
    }

    private fun observeViewModel() {
        viewModel.downloadStatus.observe(this) { status ->
            findPreference<Preference>("download_sprites")?.summary = status
        }

        viewModel.isDownloading.observe(this) { isDownloading ->
            findPreference<Preference>("download_sprites")?.isEnabled = !isDownloading
        }
    }

    private fun getDirSize(dir: java.io.File): Long {
        if (!dir.exists()) return 0
        var result: Long = 0
        val fileList = dir.listFiles()
        if (fileList != null) {
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    result += getDirSize(fileList[i])
                } else {
                    result += fileList[i].length()
                }
            }
        }
        return result
    }
}
