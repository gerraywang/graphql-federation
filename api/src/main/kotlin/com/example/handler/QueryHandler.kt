package com.example.handler

import com.example.GraphQLService
import com.example.config.AppConfig
import com.example.model.*
import com.example.util.QueryBuilder

abstract class BaseQueryHandler(
    protected val graphQLService: GraphQLService,
    protected val appConfig: AppConfig
) {
    protected fun getQueryName(tableName: String): String {
        return if (tableName in listOf("customer", "product")) {
            "queryMstTable"
        } else {
            "queryTranTable"
        }
    }

    protected fun parseConditionsFromParams(params: Map<String, List<String>>): List<Condition> {
        return params.entries.mapNotNull { entry ->
            val parts = entry.key.split("_", limit = 2)
            if (parts.size == 2) {
                Condition(
                    field = parts[0],
                    operator = OperatorType.valueOf(parts[1].uppercase()),
                    value = entry.value.firstOrNull()
                )
            } else null
        }
    }
} 