package com.example.plugins.models

import io.ktor.server.application.*

enum class Keys {
    Db, Jwt
}

fun getVar(environment: ApplicationEnvironment, keys: Keys, key: String): String = when (keys) {
    Keys.Db -> environment.config.property("ktor.db.$key").getString()
    Keys.Jwt -> environment.config.property("ktor.jwt.$key").getString()
}

class Vars(environment: ApplicationEnvironment) {
    val dbUrl = getVar(environment, Keys.Db, "dbUrl")
    val dbName = getVar(environment, Keys.Db, "dbName")
    val userCol = getVar(environment, Keys.Db, "userCol")
    val profileCol = getVar(environment, Keys.Db, "profileCol")
    val secret = getVar(environment, Keys.Jwt, "secret")
    val issuer = getVar(environment, Keys.Jwt, "issuer")
    val audience = getVar(environment, Keys.Jwt, "audience")
    val myRealm = getVar(environment, Keys.Jwt, "realm")
}