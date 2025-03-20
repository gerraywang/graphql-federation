package com.example.order

import io.ktor.server.application.*
import io.ktor.server.routing.*
import com.example.handler.CommonQueryHandler

fun Route.orderRoutes(commonQueryHandler: CommonQueryHandler) {
    route("/orders") {
        get {
            commonQueryHandler.handleQuery(
                queryName = "order-customer",
                conditions = call.request.queryParameters.entries().associate { 
                    it.key to listOf(it.value.first()) 
                },
                call = call
            )
        }

        get("/payment") {
            commonQueryHandler.handleQuery(
                queryName = "order-payment",
                conditions = call.request.queryParameters.entries().associate { 
                    it.key to listOf(it.value.first()) 
                },
                call = call
            )
        }
    }
} 