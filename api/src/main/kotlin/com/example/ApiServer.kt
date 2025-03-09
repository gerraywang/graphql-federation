package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import io.ktor.server.routing.*
import com.example.config.TableRelationsLoader
import com.example.config.QueryDefinitions
import com.example.handler.CommonQueryHandler
import com.example.order.orderRoutes

fun main() {
    val tableRelations = TableRelationsLoader.load()
    val queryDefinitions = QueryDefinitions()
    
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson()
        }
        install(CORS) {
            anyHost()
        }
        
        val graphQLService = GraphQLService()
        val commonQueryHandler = CommonQueryHandler(graphQLService, tableRelations, queryDefinitions)
        
        routing {
            // 通用查询路由
            with(ApiResource(commonQueryHandler)) { 
                apiRoutes()
            }
            
            // 订单专用路由
            orderRoutes(commonQueryHandler)
        }
    }.start(wait = true)
} 