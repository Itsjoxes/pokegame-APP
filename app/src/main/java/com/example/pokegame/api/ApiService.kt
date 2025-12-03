package com.example.pokegame.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("pokemon/{id}")
    suspend fun getPokemonInfo(@Path("id") id: Int): Pokemon

    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int, @Query("offset") offset: Int): PokeApiResponse

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpecies
}