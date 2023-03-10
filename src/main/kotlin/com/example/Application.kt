package com.example

import com.example.plugins.*
import com.example.plugins.impls.DbImpl
import com.example.plugins.impls.JwtImpl
import com.example.plugins.models.JwtConfig
import com.example.plugins.models.Vars
import com.example.plugins.routings.*
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module(testing: Boolean = false) {
    val vars = Vars(environment)
    val jwtConfig = JwtConfig(vars.secret, vars.issuer, vars.audience, vars.myRealm)
    val dbImpl = DbImpl(vars.dbUrl, vars.dbName, vars.userCol, vars.profileCol)
    val jwtImpl = JwtImpl(jwtConfig, dbImpl)

    configureOpenApi()
    configureCors()
    configureJwt(jwtConfig)
    configureSerialization()
    configureLogger()
    configureAuthRouting(dbImpl, jwtImpl)
    configureRouting(dbImpl, jwtImpl)
}
