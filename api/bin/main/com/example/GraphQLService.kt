package com.example

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.Entity
import jakarta.ws.rs.core.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@ApplicationScoped
class GraphQLService {
    private val client = ClientBuilder.newClient()
    private val objectMapper = ObjectMapper()
    private val routerUrl = "http://localhost:4000/graphql"

    fun executeQuery(query: String): Map<String, Any?> {
        val request = mapOf(
            "query" to query
        )

        val response = client.target(routerUrl)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON))

        return objectMapper.readValue(response.readEntity(String::class.java))
    }
} 