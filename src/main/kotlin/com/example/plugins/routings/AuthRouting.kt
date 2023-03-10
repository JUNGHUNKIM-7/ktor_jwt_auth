package com.example.plugins.routings

import com.example.plugins.impls.DbImpl
import com.example.plugins.impls.JwtImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

fun Application.configureAuthRouting(dbImpl: DbImpl, jwtImpl: JwtImpl) {
    runBlocking {
        routing {
            authenticate("at") {
                route("auth") {
                    post("logout") {
                        val token = jwtImpl.parseToken(call) ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            "payload might be null or error"
                        )
                        val email = token.userEmail

                        dbImpl.updateRtToken(email = email, rt = null)
                        call.respondText("logout $email! Token is expired at ${token.expiration}")
                    }
                }
            }
            authenticate("rt") {
                route("auth") {
                    post("refresh") {
                        jwtImpl.refreshTokens(call)
                        call.respond(HttpStatusCode.OK, "token refreshed!")
                    }
                }
            }
        }
    }
}