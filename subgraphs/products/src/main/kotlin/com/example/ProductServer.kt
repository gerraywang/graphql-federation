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

data class Product(val id: String, val name: String, val price: Float, val owner: User)
data class User(val id: String)

fun SchemaBuilder.productSchema() {
    query("products") {
        resolver { ->
            listOf(
                Product("101", "Laptop", 999.99f, User("1")),
                Product("102", "Phone", 499.99f, User("2"))
            )
        }
    }

    type<Product> {
        description = "Product type"
        
        property(Product::id) {
            description = "Product ID"
        }
        
        property(Product::name) {
            description = "Product name"
        }
        
        property(Product::price) {
            description = "Product price"
        }
        
        property(Product::owner) {
            description = "Product owner"
        }
    }

    type<User> {
        description = "User type"
        
        property(User::id) {
            description = "User ID"
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 4002) {
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
                productSchema()
            }
        }
        routing {
            get("/") {
                call.respondText("Products Subgraph Running")
            }
        }
    }.start(wait = true)
}
