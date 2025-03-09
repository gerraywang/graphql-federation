package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import io.ktor.server.routing.*
import com.example.config.TableRelationsLoader

fun main() {
    val tableRelations = TableRelationsLoader.load()
    
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson()
        }
        install(CORS) {
            anyHost()
        }
        
        val graphQLService = GraphQLService()
        val apiResource = ApiResource(graphQLService, tableRelations)
        
        routing {
            with(apiResource) { 
                apiRoutes()
            }
        }
    }.start(wait = true)
} 