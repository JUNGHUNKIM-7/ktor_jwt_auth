package com.example.plugins.routings

import com.example.plugins.impls.DbImpl
import com.example.plugins.impls.JwtImpl
import com.example.plugins.impls.Payload
import com.example.plugins.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

fun Application.configureRouting(dbImpl: DbImpl, jwtImpl: JwtImpl) {
    runBlocking {
        routing {
            route("auth") {
                post("register") {
                    val user = call.receive<UserDto>()
                    val (u, p) = user
                    val (at, rt) = jwtImpl.getTokenPairs(Payload(userEmail = user.user.email))

                    dbImpl.insertUser(User(email = u.email, password = u.password, rt = rt))
                    dbImpl.insertProfile(
                        Profile(firstName = p.firstName, lastName = p.lastName, userEmail = u.email)
                    )
                    jwtImpl.throwCookies(call, at = at, rt = rt)
                    call.response.status(HttpStatusCode.Created)
                }

                post("login") {
                    val user = call.receive<User>()
                    val (at, rt) = jwtImpl.getTokenPairs(Payload(userEmail = user.email))

                    dbImpl.updateRtToken(email = user.email, rt = rt)
                    jwtImpl.throwCookies(call, at = at, rt = rt)
                    call.response.status(HttpStatusCode.OK)
                }
            }
        }
    }
}
