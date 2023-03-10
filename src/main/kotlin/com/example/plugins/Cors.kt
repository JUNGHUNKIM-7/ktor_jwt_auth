package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        allowHost("client-host")
        allowHost("client-host:8080")
        allowHost("client-host", subDomains = listOf("en", "de", "es"))
        allowHost("client-host", schemes = listOf("http", "https"))
    }
}
