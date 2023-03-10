package com.example.plugins.models

data class JwtConfig(val secret: String, val issuer: String, val audience: String, val myRealm: String)