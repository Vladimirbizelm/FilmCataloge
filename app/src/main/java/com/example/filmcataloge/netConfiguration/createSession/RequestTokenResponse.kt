package com.example.filmcataloge.netConfiguration.createSession

data class RequestTokenResponse(
    val expires_at: String,
    val request_token: String,
    val success: Boolean
)