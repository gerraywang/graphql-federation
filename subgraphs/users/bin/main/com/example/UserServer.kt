package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder

data class User(
    val id: String,
    val name: String,
    val email: String
)

fun SchemaBuilder.userSchema() {
    query("users") {
        resolver { ->
            listOf(
                User("1", "John Doe", "john@example.com"),
                User("2", "Jane Doe", "jane@example.com")
            )
        }
    }

    type<User> {
        description = "User type"
        
        property(User::id) {
            description = "User ID"
        }
        
        property(User::name) {
            description = "User name"
        }
        
        property(User::email) {
            description = "User email"
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 4001) {
        install(ContentNegotiation) {
            gson()
        }
        install(CORS) {
            anyHost()
        }
        install(GraphQL) {
            playground = true
            endpoint = "/graphql"
            schema {
                userSchema()
            }
        }
        routing {
            get("/") {
                call.respondText("Users Subgraph Running")
            }
        }
    }.start(wait = true)
}
