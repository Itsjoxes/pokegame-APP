package com.example.pokegame.api

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UserRetrofitClient {
        private const val BASE_URL = "https://pokedex-app-omb3.onrender.com/"
        var service: UserService? = null

        fun getInstance(context: android.content.Context): UserService {
                if (service == null) {
                        val sessionManager = com.example.pokegame.util.SessionManager(context)
                        val client =
                                OkHttpClient.Builder()
                                        .addInterceptor(BasicAuthInterceptor(sessionManager))
                                        .connectTimeout(120, TimeUnit.SECONDS)
                                        .readTimeout(120, TimeUnit.SECONDS)
                                        .writeTimeout(120, TimeUnit.SECONDS)
                                        .build()

                        val retrofit =
                                Retrofit.Builder()
                                        .baseUrl(BASE_URL)
                                        .client(client)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()

                        service = retrofit.create(UserService::class.java)
                }
                return service!!
        }
}
