package com.example.plugins.impls

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.plugins.models.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.util.*

data class Payload(val userEmail: String)
data class ReturnedToken(val userEmail: String, val expiration: Date)

enum class TokenType {
    At, Rt
}

class JwtImpl(
    private val jwtConfig: JwtConfig,
    private val dbImpl: DbImpl
) {
    companion object {
        fun genExpiration(): Pair<Instant, Instant> {
            val now = Clock.System.now()
            val systemTZ = TimeZone.currentSystemDefault()
            val atExpiration = now.plus(DateTimePeriod(hours = 1), systemTZ)
            val rtExpiration = now.plus(DateTimePeriod(months = 6), systemTZ)
            return atExpiration to rtExpiration
        }
    }

    private fun genTokens(payload: Payload, tokenType: TokenType): String {
        val (atExpiration, rtExpiration) = genExpiration()

        val dateOfExpiration = when (tokenType) {
            TokenType.At -> atExpiration
            TokenType.Rt -> rtExpiration
        }

        return JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("email", payload.userEmail)
            .withExpiresAt(Date(dateOfExpiration.toEpochMilliseconds()))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
    }

    fun getTokenPairs(payload: Payload): List<String> {
        val at =
            genTokens(payload = payload, tokenType = TokenType.At)
        val rt =
            genTokens(payload = payload, tokenType = TokenType.Rt)

        return listOf(at, rt)
    }

    fun parseToken(call: ApplicationCall): ReturnedToken? {
        val principal = call.principal<JWTPrincipal>() ?: return null
        val expiration = principal.payload.expiresAt
        val email = principal.payload.getClaim("email")?.asString()!!

        return ReturnedToken(email, expiration)
    }

    fun throwCookies(call: ApplicationCall, at: String, rt: String) {
        call.response.cookies.append(
            Cookie(
                name = "at",
                value = at,
            )
        )
        call.response.cookies.append(
            Cookie(
                name = "rt",
                value = rt,
            )
        )
    }

    //should checked firstly, when access auth routing
    suspend fun continueAtToken(call: ApplicationCall, expiration: Date) {
        val now = Clock.System.now()
        val systemTZ = TimeZone.currentSystemDefault()
        val parsed = Instant.fromEpochMilliseconds(expiration.time)

        //shouldRefreshed < now < unauthorized(due)
        val shouldRefreshed = parsed.minus(DateTimePeriod(minutes = 30), systemTZ)
        if (now > shouldRefreshed) {
            refreshTokens(call)
        }
    }

    suspend fun refreshTokens(call: ApplicationCall) {
        //todo rt token validation
        val token = parseToken(call) ?: return
        val (at, rt) = getTokenPairs(Payload(userEmail = token.userEmail))
        dbImpl.updateRtToken(email = token.userEmail, rt = rt)
        throwCookies(call, at = at, rt = rt)
    }
}

