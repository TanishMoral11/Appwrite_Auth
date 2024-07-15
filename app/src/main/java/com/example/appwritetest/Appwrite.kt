package com.example.appwritetest


import android.content.Context
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.*
import io.appwrite.services.*

object Appwrite {
    lateinit var client: Client
    lateinit var account: Account

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("6694f179000fe9302ee5")

        account = Account(client)
    }

    suspend fun onLogin(
        email: String,
        password: String,
    ): Session {
        return account.createEmailPasswordSession(
            email,
            password,
        )
    }

    suspend fun onRegister(
        email: String,
        password: String,
    ): User<Map<String, Any>> {
        return account.create(
            userId = ID.unique(),
            email,
            password,
        )
    }

    suspend fun onLogout() {
        account.deleteSession("current")
    }
}
