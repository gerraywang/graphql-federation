package com.example

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*

class GraphQLService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }
    private val routerUrl = "http://localhost:4000/graphql"

    suspend fun executeQuery(query: String): String {
        val response = client.post(routerUrl) {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "query" to query,
                "variables" to emptyMap<String, Any>()
            ))
        }
        return response.bodyAsText()
    }
} 