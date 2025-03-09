package com.example.handler

import com.example.GraphQLService
import com.example.config.TableRelations
import com.example.config.QueryDefinitions
import com.example.config.QueryDefinition
import com.example.model.*
import com.example.util.QueryBuilder
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*

class CommonQueryHandler(
    private val graphQLService: GraphQLService,
    private val tableRelations: TableRelations,
    private val queryDefinitions: QueryDefinitions
) {
    suspend fun handleQuery(queryName: String, conditions: Map<String, List<String>>, call: ApplicationCall) {
        try {
            // 获取查询定义
            val definition = queryDefinitions.getDefinition(queryName)
                ?: throw IllegalArgumentException("Unknown query: $queryName")
            
            // 解析条件
            val parsedConditions = parseConditionsFromParams(conditions)
            
            // 构建查询
            val query = buildQueryFromDefinition(definition, parsedConditions)
            
            // 执行查询
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
        val relation = tableRelations.getRelation(definition.relationName)
            ?: throw IllegalArgumentException("Unknown relation: ${definition.relationName}")

        // 构建主表查询
        val mainQuery = QueryBuilder.buildTableQuery(
            definition.mainTable,
            getQueryName(definition.mainTable.tableName),
            "main"
        )

        // 构建关联表查询
        val joinQueries = relation.joinTables.map { joinTable ->
            val joinConfig = definition.joins.find { it.tableName == joinTable.table }
            if (joinConfig != null) {
                val tableConditions = conditions.filter { it.field in joinConfig.fields }
                val updatedConfig = joinConfig.copy(conditions = tableConditions)
                QueryBuilder.buildTableQuery(updatedConfig.toTableQuery(), getQueryName(joinConfig.tableName))
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