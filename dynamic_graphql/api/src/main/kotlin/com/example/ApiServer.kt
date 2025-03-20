package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import io.ktor.server.routing.*
import com.example.config.AppConfigLoader
import com.example.handler.CommonQueryHandler
import com.example.order.orderRoutes

fun main() {
    val appConfig = AppConfigLoader.load()
    
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson()
        }
        install(CORS) {
            anyHost()
        }
        
        val graphQLService = GraphQLService()
        val commonQueryHandler = CommonQueryHandler(graphQLService, appConfig)
        
        routing {
            // common query endpoint
            with(ApiResource(commonQueryHandler)) { 
                apiRoutes()
            }
            
            // order endpoint
            orderRoutes(commonQueryHandler)
        }
    }.start(wait = true)
} 