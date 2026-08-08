package com.travelplanner.api

import android.util.Base64
import com.travelplanner.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class RttAuthInterceptor(private val settingsRepository: SettingsRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // Read credentials synchronously on the background OkHttp thread
        val username = runBlocking { settingsRepository.apiUsernameFlow.first() }.trim()
        val password = runBlocking { settingsRepository.apiPasswordFlow.first() }.trim()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            // Use Basic Auth if both username and password are provided
            val credentials = "$username:$password"
            val base64Credentials = Base64.encodeToString(
                credentials.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            builder.header("Authorization", "Basic $base64Credentials")
        } else if (password.isNotEmpty()) {
            // Use Bearer Token if only password/token is provided
            builder.header("Authorization", "Bearer $password")
        }

        return chain.proceed(builder.build())
    }
}
