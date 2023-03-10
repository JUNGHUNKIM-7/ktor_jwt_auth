package com.example.plugins.impls

import com.example.plugins.models.Profile
import com.example.plugins.models.User
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue


enum class Coll {
    User, Profile
}

open class Db(dbUrl: String, dbName: String, val userCol: String, val profileCol: String) {
    private val client =
        KMongo.createClient(dbUrl).coroutine
    val database = client.getDatabase(dbName)

    inline fun <reified T : Any> getColl(coll: Coll): CoroutineCollection<T> = when (coll) {
        Coll.User -> database.getCollection(userCol)
        Coll.Profile -> database.getCollection(profileCol)
    }
}

class DbImpl(dbUrl: String, dbName: String, userCol: String, profileCol: String) :
    Db(dbUrl, dbName, userCol, profileCol) {

    suspend fun insertUser(user: User) {
        super.getColl<User>(Coll.User).insertOne(user)
    }

    suspend fun insertProfile(profile: Profile) {
        super.getColl<Profile>(Coll.Profile).insertOne(profile)
    }

    suspend fun updateRtToken(email: String, rt: String?) {
        super.getColl<User>(Coll.User).updateOne(User::email eq email, setValue(User::rt, rt))
    }
}