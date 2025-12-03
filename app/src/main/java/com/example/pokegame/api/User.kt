package com.example.pokegame.api

import com.google.gson.annotations.SerializedName

data class Usuario(
        val username: String,
        @SerializedName("nombre") val fullName: String? = null,
        val email: String? = null,
        val contrasena: String? = null
)
