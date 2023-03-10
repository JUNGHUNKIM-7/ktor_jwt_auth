package com.example.plugins.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val user: User,
    val profile: Profile
)

@Serializable
data class User(
    val email: String,
    val password: String,
    val rt: String? = null,
)

@Serializable
data class Profile(
    val firstName: String,
    val lastName: String,
    val userEmail: String? = null,
)
