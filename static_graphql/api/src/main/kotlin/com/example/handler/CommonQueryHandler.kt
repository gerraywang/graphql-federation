package com.example.handler

import com.example.GraphQLService
import com.example.config.AppConfig
import com.example.config.QueryDefinition
import com.example.config.JoinTable
import com.example.model.*
import com.example.util.QueryBuilder
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*

class CommonQueryHandler(
    private val graphQLService: GraphQLService,
    private val appConfig: AppConfig
) {
    suspend fun handleQuery(queryName: String, conditions: Map<String, List<String>>, call: ApplicationCall) {
        try {
            val definition = appConfig.getQueryDefinition(queryName)
                ?: throw IllegalArgumentException("Unknown query: $queryName")
            
            // Parse conditions
            val parsedConditions = parseConditionsFromParams(conditions)
            
            // Build query
            val query = buildQueryFromDefinition(definition, parsedConditions)
            
            // Execute query
            val result = graphQLService.executeQuery(query)
            call.respond(result)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }

    private fun parseConditionsFromParams(params: Map<String, List<String>>): List<Condition> {
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

    private fun buildQueryFromDefinition(
        definition: QueryDefinition,
        conditions: List<Condition>
    ): String {
        val relation = appConfig.getRelation(definition.relationName)
            ?: throw IllegalArgumentException("Unknown relation: ${definition.relationName}")

        // Build main table query
        val mainQuery = QueryBuilder.buildTableQuery(
            definition.mainTable.toTableQuery(),
            getQueryName(definition.mainTable.tableName),
            "main"
        )

        // Build join table queries
        val joinQueries = relation.joinTables.map { joinTable ->
            val joinConfig = definition.joins.find { it.tableName == joinTable.table }
            if (joinConfig != null) {
                val tableConditions = conditions.filter { it.field in joinConfig.fields }
                val joinTableQuery = JoinTableQuery(
                    tableName = joinConfig.tableName,
                    fields = joinConfig.fields,
                    alias = joinConfig.alias,
                    conditions = tableConditions
                ).toTableQuery()
                QueryBuilder.buildTableQuery(joinTableQuery, getQueryName(joinConfig.tableName))
            } else {
                QueryBuilder.buildTableQuery(
                    TableQuery(
                        tableName = joinTable.table,
                        fields = listOf("*"),
                        alias = joinTable.table
                    ),
                    getQueryName(joinTable.table)
                )
            }
        }.joinToString("\n")

        return """
            query {
                $mainQuery
                $joinQueries
            }
        """.trimIndent()
    }

    private fun getQueryName(tableName: String): String {
        return if (tableName in listOf("customer", "product")) {
            "queryMstTable"
        } else {
            "queryTranTable"
        }
    }
} 