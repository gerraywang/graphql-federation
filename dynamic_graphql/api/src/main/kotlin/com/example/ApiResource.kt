package com.example

import io.ktor.server.application.*
import io.ktor.server.routing.*
import com.example.handler.CommonQueryHandler

class ApiResource(
    private val commonQueryHandler: CommonQueryHandler
) {
    fun Route.apiRoutes() {
        // common query endpoint
        get("/api/{queryName}") {
            val queryName = call.parameters["queryName"]!!
            commonQueryHandler.handleQuery(
                queryName = queryName,
                conditions = call.request.queryParameters.entries().associate { 
                    it.key to listOf(it.value.first()) 
                },
                call = call
            )
        }
    }
} 