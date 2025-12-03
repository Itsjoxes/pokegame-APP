package com.example.pokegame.api

import com.example.pokegame.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        val path = original.url.encodedPath
        // No enviar token en login ni registro
        if (!path.endsWith("/login") && !path.endsWith("/usuarios")) {
            val token = sessionManager.getToken()
            if (token != null) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(builder.build())
    }
}
