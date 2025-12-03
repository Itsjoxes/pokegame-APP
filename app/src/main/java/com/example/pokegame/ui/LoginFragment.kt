package com.example.pokegame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pokegame.api.LoginRequest
import com.example.pokegame.api.UserRetrofitClient
import com.example.pokegame.api.Usuario
import com.example.pokegame.databinding.FragmentLoginBinding
import com.example.pokegame.util.SessionManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var isLoginMode = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        updateModeUI()
    }

    private fun setupListeners() {
        binding.tvSwitchMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateModeUI()
        }

        binding.tvBottomSwitch.setOnClickListener {
            isLoginMode = !isLoginMode
            updateModeUI()
        }

        binding.btnAction.setOnClickListener {
            if (validateInput()) {
                performAction()
            }
        }
    }

    private fun updateModeUI() {
        if (isLoginMode) {
            binding.tvTitle.text = "INICIAR SESIÓN"
            binding.tvFullName.visibility = View.GONE
            binding.etFullName.visibility = View.GONE
            binding.tvEmail.visibility = View.GONE
            binding.etEmail.visibility = View.GONE
            binding.tvConfirmPassword.visibility = View.GONE
            binding.etConfirmPassword.visibility = View.GONE
            binding.btnAction.text = "Iniciar sesión"
            binding.tvSwitchMode.text = "Registrarse"
            binding.tvBottomSwitch.text = "¿No tienes cuenta? Regístrate"
        } else {
            binding.tvTitle.text = "REGÍSTRATE"
            binding.tvFullName.visibility = View.VISIBLE
            binding.etFullName.visibility = View.VISIBLE
            binding.tvEmail.visibility = View.VISIBLE
            binding.etEmail.visibility = View.VISIBLE
            binding.tvConfirmPassword.visibility = View.VISIBLE
            binding.etConfirmPassword.visibility = View.VISIBLE
            binding.btnAction.text = "Registrarse"
            binding.tvSwitchMode.text = "Ir a iniciar sesión"
            binding.tvBottomSwitch.text = "¿Ya tienes cuenta? Inicia sesión"
        }
    }

    private fun validateInput(): Boolean {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.etUsername.error = "Campo requerido"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Campo requerido"
            return false
        }

        if (!isLoginMode) {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.etFullName.error = "Campo requerido"
                return false
            }

            if (email.isEmpty()) {
                binding.etEmail.error = "Campo requerido"
                return false
            }

            if (confirmPassword.isEmpty()) {
                binding.etConfirmPassword.error = "Campo requerido"
                return false
            }

            if (password != confirmPassword) {
                binding.etConfirmPassword.error = "Las contraseñas no coinciden"
                return false
            }
        }

        return true
    }

    private fun performAction() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.progressBar.visibility = View.VISIBLE
        binding.btnAction.isEnabled = false

        lifecycleScope.launch {
            try {
                if (isLoginMode) {
                    val service = UserRetrofitClient.getInstance(requireContext())
                    val response = service.login(LoginRequest(username, password))

                    if (response.isSuccessful && response.body() != null) {
                        val token = response.body()!!.token
                        sessionManager.saveToken(token)
                        sessionManager.saveSession(username, password) // Keep saving username/password if needed for other things, or just username
                        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT)
                                .show()
                        findNavController().popBackStack()
                    } else {
                        // Clear session if failed
                        sessionManager.clearSession()
                        when (response.code()) {
                            404 ->
                                    Toast.makeText(
                                                    context,
                                                    "Usuario no encontrado",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                            401 -> {
                                    val errorMsg = response.errorBody()?.string() ?: "Credenciales inválidas"
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Error 401")
                                        .setMessage(errorMsg)
                                        .setPositiveButton("OK", null)
                                        .show()
                            }
                            403 -> {
                                    val errorMsg = response.errorBody()?.string() ?: "Acceso denegado"
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Error 403")
                                        .setMessage(errorMsg)
                                        .setPositiveButton("OK", null)
                                        .show()
                            }
                            else -> {
                                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Error ${response.code()}")
                                        .setMessage(errorMsg)
                                        .setPositiveButton("OK", null)
                                        .show()
                            }
                        }
                    }
                } else {
                    val fullName = binding.etFullName.text.toString().trim()
                    val email = binding.etEmail.text.toString().trim()
                    val usuario = Usuario(username, fullName, email, password)

                    // For registration, we might not need auth, or we might.
                    // Assuming public for now, but if it fails with 401, we know why.
                    // If registration logs you in, we should save session.

                    val service = UserRetrofitClient.getInstance(requireContext())
                    val response = service.createUsuario(usuario)

                    if (response.isSuccessful && response.body() != null) {
                        sessionManager.saveSession(username, password)
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        when (response.code()) {
                            403 ->
                                    Toast.makeText(
                                                    context,
                                                    "Acceso denegado (403).",
                                                    Toast.LENGTH_LONG
                                            )
                                            .show()
                            else -> {
                                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Error registro ${response.code()}")
                                        .setMessage(errorMsg)
                                        .setPositiveButton("OK", null)
                                        .show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                sessionManager.clearSession()
                AlertDialog.Builder(requireContext())
                        .setTitle("Error de conexión")
                        .setMessage(e.toString())
                        .setPositiveButton("OK", null)
                        .show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnAction.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
