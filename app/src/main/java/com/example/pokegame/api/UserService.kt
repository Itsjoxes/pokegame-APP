package com.example.pokegame.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @GET("usuarios/{username}")
    suspend fun getUsuarioByUsername(@Path("username") username: String): Response<Usuario>

    @POST("usuarios") suspend fun createUsuario(@Body usuario: Usuario): Response<Usuario>

    @POST("login") suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("usuarios/{username}/pokemons")
    suspend fun capturePokemon(
            @Path("username") username: String,
            @Body pokemon: Pokemon
    ): Response<Pokemon>

    @GET("usuarios/{username}/pokemons")
    suspend fun getCapturedPokemons(@Path("username") username: String): Response<List<Pokemon>>
}
