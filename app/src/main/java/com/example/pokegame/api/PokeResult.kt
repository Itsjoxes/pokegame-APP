package com.example.pokegame.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PokeResult (
    @Expose @SerializedName("name") val name: String,
    @Expose @SerializedName("url") val url: String
)